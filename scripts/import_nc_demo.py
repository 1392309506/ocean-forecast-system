#!/usr/bin/env python
"""Import monthly NetCDF data into a local SQLite DB and export demo JSON.

Usage example:
python scripts/import_nc_demo.py \
  --nc backend/src/main/resources/glo12v1_monthly_2013_05.nc \
  --db backend/src/main/resources/ocean_demo.db \
  --json frontend/public/demo/monthly-grid-demo.json
"""

from __future__ import annotations

import argparse
import json
import math
import sqlite3
from datetime import datetime
from pathlib import Path
from typing import Iterable

import numpy as np
import xarray as xr


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Import NetCDF to SQLite + demo JSON")
    parser.add_argument(
        "--nc",
        default="backend/src/main/resources/glo12v1_monthly_2013_05.nc",
        help="Path to source NetCDF file",
    )
    parser.add_argument(
        "--db",
        default="backend/src/main/resources/ocean_demo.db",
        help="Path to target SQLite DB file",
    )
    parser.add_argument(
        "--json",
        default="frontend/public/demo/monthly-grid-demo.json",
        help="Path to generated demo JSON file",
    )
    parser.add_argument(
        "--var",
        default="",
        help="Variable name in NetCDF to use. If omitted, auto-detect.",
    )
    parser.add_argument(
        "--stride",
        type=int,
        default=8,
        help="Subsample stride for demo export/import (higher = fewer points).",
    )
    parser.add_argument(
        "--max-points",
        type=int,
        default=20000,
        help="Hard cap for inserted grid points.",
    )
    return parser.parse_args()


def find_coord_name(ds: xr.Dataset, candidates: Iterable[str]) -> str:
    lower_map = {name.lower(): name for name in ds.coords}
    for cand in candidates:
        if cand in lower_map:
            return lower_map[cand]
    for name in ds.coords:
        lname = name.lower()
        for cand in candidates:
            if cand in lname:
                return name
    raise ValueError(f"Cannot find coordinate from candidates: {list(candidates)}")


def pick_data_var(ds: xr.Dataset, lon_name: str, lat_name: str, wanted: str) -> xr.DataArray:
    if wanted:
        if wanted not in ds.variables:
            raise ValueError(f"Variable '{wanted}' not found. Available: {list(ds.data_vars)}")
        da = ds[wanted]
        if lon_name not in da.dims or lat_name not in da.dims:
            raise ValueError(f"Variable '{wanted}' does not include lon/lat dims")
        return da

    preferred_tokens = ("thetao", "sst", "temp", "temperature", "so", "salinity")
    candidates: list[xr.DataArray] = []
    for name, da in ds.data_vars.items():
        if lon_name in da.dims and lat_name in da.dims and np.issubdtype(da.dtype, np.number):
            candidates.append(da)

    if not candidates:
        raise ValueError("No numeric variable found with lon/lat dims")

    for da in candidates:
        lname = da.name.lower()
        if any(token in lname for token in preferred_tokens):
            return da

    return max(candidates, key=lambda x: x.size)


def squeeze_to_2d(da: xr.DataArray, lon_name: str, lat_name: str) -> xr.DataArray:
    indexers = {}
    for dim in da.dims:
        if dim not in (lon_name, lat_name):
            indexers[dim] = 0
    da2d = da.isel(**indexers)
    if da2d.ndim != 2:
        raise ValueError(f"Expected 2D grid after squeeze, got dims={da2d.dims}")
    if da2d.dims != (lat_name, lon_name):
        da2d = da2d.transpose(lat_name, lon_name)
    return da2d


def infer_timestamp(ds: xr.Dataset) -> str:
    for name in ds.coords:
        lname = name.lower()
        if "time" in lname and ds.coords[name].size > 0:
            v = ds.coords[name].values[0]
            try:
                return str(np.datetime_as_string(v, unit="s"))
            except Exception:
                return str(v)
    return datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")


def ensure_schema(conn: sqlite3.Connection) -> None:
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS ocean_grid_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            longitude REAL NOT NULL,
            latitude REAL NOT NULL,
            data_type TEXT NOT NULL,
            data_value REAL NOT NULL,
            unit TEXT NOT NULL,
            data_time TEXT NOT NULL
        );

        CREATE INDEX IF NOT EXISTS idx_ocean_grid_data_type_time
        ON ocean_grid_data(data_type, data_time);
        """
    )
    conn.commit()


def main() -> None:
    args = parse_args()

    nc_path = Path(args.nc)
    db_path = Path(args.db)
    json_path = Path(args.json)

    if not nc_path.exists():
        raise FileNotFoundError(f"NetCDF file not found: {nc_path.resolve()}")

    ds = xr.open_dataset(nc_path)
    lon_name = find_coord_name(ds, ("lon", "longitude", "x"))
    lat_name = find_coord_name(ds, ("lat", "latitude", "y"))

    data_var = pick_data_var(ds, lon_name, lat_name, args.var)
    da2d = squeeze_to_2d(data_var, lon_name, lat_name)

    stride = max(1, int(args.stride))
    da2d = da2d.isel({lat_name: slice(None, None, stride), lon_name: slice(None, None, stride)})

    lat = da2d[lat_name].values.astype(float)
    lon = da2d[lon_name].values.astype(float)
    values = da2d.values.astype(float)

    valid = values[np.isfinite(values)]
    if valid.size == 0:
        raise ValueError("No valid numeric values found in selected variable")

    data_type = str(data_var.name)
    unit = str(data_var.attrs.get("units", "unknown"))
    timestamp = infer_timestamp(ds)

    payload = {
        "lon": lon.tolist(),
        "lat": lat.tolist(),
        "values": values.tolist(),
        "dataType": data_type,
        "unit": unit,
        "minValue": float(np.nanmin(valid)),
        "maxValue": float(np.nanmax(valid)),
        "timestamp": timestamp,
    }

    json_path.parent.mkdir(parents=True, exist_ok=True)
    json_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

    db_path.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(db_path)
    try:
        ensure_schema(conn)

        rows = []
        for i, lat_v in enumerate(lat):
            for j, lon_v in enumerate(lon):
                value = float(values[i, j])
                if not math.isfinite(value):
                    continue
                rows.append((float(lon_v), float(lat_v), data_type, value, unit, timestamp))
                if len(rows) >= args.max_points:
                    break
            if len(rows) >= args.max_points:
                break

        conn.executemany(
            """
            INSERT INTO ocean_grid_data (longitude, latitude, data_type, data_value, unit, data_time)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            rows,
        )
        conn.commit()
    finally:
        conn.close()
        ds.close()

    print(f"NetCDF: {nc_path}")
    print(f"Variable: {data_type}, unit={unit}")
    print(f"Exported JSON: {json_path}")
    print(f"Inserted rows: {len(rows)} into DB: {db_path}")


if __name__ == "__main__":
    main()
