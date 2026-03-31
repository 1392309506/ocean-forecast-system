#!/usr/bin/env python
"""Migrate demo grid data from SQLite to MySQL.

Example:
  D:/Code_store/ocean-forecast-system/.venv/Scripts/python.exe scripts/sqlite_to_mysql_demo.py \
    --sqlite backend/src/main/resources/ocean_demo.db \
    --host 127.0.0.1 --port 3306 --user root --password your_password \
    --database ocean_demo
"""

from __future__ import annotations

import argparse
import sqlite3
from pathlib import Path

import pymysql


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Migrate ocean_grid_data from SQLite to MySQL")
    parser.add_argument(
        "--sqlite",
        default="backend/src/main/resources/ocean_demo.db",
        help="Path to SQLite database file",
    )
    parser.add_argument("--host", default="127.0.0.1", help="MySQL host")
    parser.add_argument("--port", type=int, default=3306, help="MySQL port")
    parser.add_argument("--user", required=True, help="MySQL user")
    parser.add_argument("--password", required=True, help="MySQL password")
    parser.add_argument("--database", default="ocean_demo", help="Target MySQL database")
    parser.add_argument(
        "--source-table",
        default="ocean_grid_data",
        help="Source table in SQLite",
    )
    parser.add_argument(
        "--target-table",
        default="ocean_grid_data",
        help="Target table in MySQL",
    )
    parser.add_argument(
        "--chunk-size",
        type=int,
        default=2000,
        help="Batch insert size",
    )
    parser.add_argument(
        "--truncate",
        action="store_true",
        help="Truncate target table before insert",
    )
    return parser.parse_args()


def ensure_target_schema(cur: pymysql.cursors.Cursor, table_name: str) -> None:
    cur.execute(
        f"""
        CREATE TABLE IF NOT EXISTS {table_name} (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            longitude DOUBLE NOT NULL,
            latitude DOUBLE NOT NULL,
            data_type VARCHAR(64) NOT NULL,
            data_value DOUBLE NOT NULL,
            unit VARCHAR(32) NOT NULL,
            data_time VARCHAR(64) NOT NULL,
            INDEX idx_data_type_time (data_type, data_time)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """
    )


def chunked(seq: list[tuple], size: int):
    for i in range(0, len(seq), size):
        yield seq[i : i + size]


def main() -> None:
    args = parse_args()

    sqlite_path = Path(args.sqlite)
    if not sqlite_path.exists():
        raise FileNotFoundError(f"SQLite file not found: {sqlite_path.resolve()}")

    sqlite_conn = sqlite3.connect(sqlite_path)
    sqlite_cur = sqlite_conn.cursor()
    sqlite_cur.execute(
        f"""
        SELECT longitude, latitude, data_type, data_value, unit, data_time
        FROM {args.source_table}
        """
    )
    rows = sqlite_cur.fetchall()
    sqlite_conn.close()

    if not rows:
        print("No rows found in source table. Nothing to migrate.")
        return

    admin_conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        charset="utf8mb4",
        autocommit=True,
    )
    try:
        with admin_conn.cursor() as admin_cur:
            admin_cur.execute(f"CREATE DATABASE IF NOT EXISTS {args.database} CHARACTER SET utf8mb4")
    finally:
        admin_conn.close()

    mysql_conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
        autocommit=False,
    )

    inserted = 0
    try:
        with mysql_conn.cursor() as cur:
            ensure_target_schema(cur, args.target_table)
            if args.truncate:
                cur.execute(f"TRUNCATE TABLE {args.target_table}")

            sql = (
                f"INSERT INTO {args.target_table} "
                "(longitude, latitude, data_type, data_value, unit, data_time) "
                "VALUES (%s, %s, %s, %s, %s, %s)"
            )
            for batch in chunked(rows, max(1, args.chunk_size)):
                cur.executemany(sql, batch)
                inserted += len(batch)

        mysql_conn.commit()
    finally:
        mysql_conn.close()

    print(f"Source SQLite: {sqlite_path}")
    print(f"Target MySQL: {args.host}:{args.port}/{args.database}.{args.target_table}")
    print(f"Rows migrated: {inserted}")


if __name__ == "__main__":
    main()
