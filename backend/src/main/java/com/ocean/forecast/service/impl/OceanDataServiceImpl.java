package com.ocean.forecast.service.impl;

import com.ocean.forecast.entity.ForecastResult;
import com.ocean.forecast.entity.ForecastMetaRecord;
import com.ocean.forecast.entity.GridData;
import com.ocean.forecast.entity.OceanData;
import com.ocean.forecast.entity.OceanGridRecord;
import com.ocean.forecast.entity.OceanSeriesRecord;
import com.ocean.forecast.entity.TimeSeriesData;
import com.ocean.forecast.mapper.OceanDataMapper;
import com.ocean.forecast.service.OceanDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OceanDataServiceImpl implements OceanDataService {

    private final OceanDataMapper oceanDataMapper;

    public OceanDataServiceImpl(OceanDataMapper oceanDataMapper) {
        this.oceanDataMapper = oceanDataMapper;
    }

    @Override
    public GridData getGridData(String dataType, String timestamp) {
        String resolvedTimestamp = (timestamp == null || timestamp.isBlank()) ? LocalDateTime.now().toString()
                : timestamp;
        List<OceanGridRecord> rows = oceanDataMapper.selectGridRows(dataType);
        if (rows == null || rows.isEmpty()) {
            return GridData.builder()
                    .dataType(dataType)
                    .timestamp(resolvedTimestamp)
                    .unit("")
                    .lon(new double[0])
                    .lat(new double[0])
                    .values(new double[0][0])
                    .minValue(0.0)
                    .maxValue(0.0)
                    .build();
        }

        Map<Double, Integer> latIndex = new LinkedHashMap<>();
        Map<Double, Integer> lonIndex = new LinkedHashMap<>();
        rows.stream().map(OceanGridRecord::getLatitude).distinct().sorted()
                .forEach(lat -> latIndex.put(lat, latIndex.size()));
        rows.stream().map(OceanGridRecord::getLongitude).distinct().sorted()
                .forEach(lon -> lonIndex.put(lon, lonIndex.size()));

        double[] lat = new double[latIndex.size()];
        for (Map.Entry<Double, Integer> e : latIndex.entrySet()) {
            lat[e.getValue()] = e.getKey();
        }

        double[] lon = new double[lonIndex.size()];
        for (Map.Entry<Double, Integer> e : lonIndex.entrySet()) {
            lon[e.getValue()] = e.getKey();
        }

        double[][] values = new double[lat.length][lon.length];
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (OceanGridRecord row : rows) {
            int i = latIndex.get(row.getLatitude());
            int j = lonIndex.get(row.getLongitude());
            values[i][j] = row.getValue();
            minValue = Math.min(minValue, row.getValue());
            maxValue = Math.max(maxValue, row.getValue());
        }

        return GridData.builder()
                .lon(lon)
                .lat(lat)
                .values(values)
                .dataType(dataType)
                .unit(rows.get(0).getUnit())
                .minValue(minValue)
                .maxValue(maxValue)
                .timestamp(resolvedTimestamp)
                .build();
    }

    @Override
    public TimeSeriesData getTimeSeriesData(Double longitude, Double latitude, String dataType) {
        List<OceanSeriesRecord> rows = oceanDataMapper.selectTimeSeriesRows(longitude, latitude, dataType);
        if (rows == null || rows.isEmpty()) {
            return TimeSeriesData.builder()
                    .longitude(longitude)
                    .latitude(latitude)
                    .dataType(dataType)
                    .unit("")
                    .locationName(String.format("%.2fN, %.2fE", latitude, longitude))
                    .timestamps(List.of())
                    .values(List.of())
                    .build();
        }

        rows.sort(Comparator.comparing(OceanSeriesRecord::getDataTime));
        List<String> timestamps = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (OceanSeriesRecord row : rows) {
            timestamps.add(row.getDataTime().toString());
            values.add(row.getValue());
        }

        return TimeSeriesData.builder()
                .timestamps(timestamps)
                .values(values)
                .dataType(dataType)
                .unit(rows.get(0).getUnit())
                .locationName(String.format("%.2fN, %.2fE", latitude, longitude))
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }

    @Override
    public ForecastResult getForecastResult(String region, Integer forecastHours) {
        ForecastMetaRecord meta = oceanDataMapper.selectForecastMeta(region, forecastHours);
        List<String> forecastTimes = new ArrayList<>();
        List<GridData> gridDataList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        int steps = Math.max(1, Math.min(forecastHours / 6, 5));
        for (int i = 0; i < steps; i++) {
            String forecastTime = now.plusHours(i * 6L).toString();
            forecastTimes.add(forecastTime);
            gridDataList.add(getGridData("temperature", forecastTime));
        }

        ForecastMetaRecord effectiveMeta = (meta == null) ? defaultMeta(region, forecastHours) : meta;

        return ForecastResult.builder()
                .forecastId(UUID.randomUUID().toString())
                .forecastType(effectiveMeta.getForecastType())
                .region(effectiveMeta.getRegion())
                .forecastHours(effectiveMeta.getForecastHours())
                .forecastTimes(forecastTimes)
                .gridDataList(gridDataList)
                .confidenceLower(effectiveMeta.getConfidenceLower())
                .confidenceUpper(effectiveMeta.getConfidenceUpper())
                .accuracy(effectiveMeta.getAccuracy())
                .createTime(effectiveMeta.getCreateTime().toString())
                .build();
    }

    @Override
    public OceanData getObservationData(Double longitude, Double latitude) {
        OceanData observation = oceanDataMapper.selectObservationData(longitude, latitude);
        if (observation != null) {
            return observation;
        }

        return OceanData.builder()
                .id(-1L)
                .longitude(longitude)
                .latitude(latitude)
                .seaSurfaceTemp(0.0)
                .salinity(0.0)
                .currentSpeed(0.0)
                .currentDirection(0.0)
                .waveHeight(0.0)
                .wavePeriod(0.0)
                .windSpeed(0.0)
                .windDirection(0.0)
                .dataTime(LocalDateTime.now())
                .dataSource("N/A")
                .qualityFlag(0)
                .build();
    }

    private ForecastMetaRecord defaultMeta(String region, Integer forecastHours) {
        ForecastMetaRecord record = new ForecastMetaRecord();
        record.setForecastType("海温预报");
        record.setRegion(region);
        record.setForecastHours(forecastHours);
        record.setConfidenceLower(0.85);
        record.setConfidenceUpper(0.95);
        record.setAccuracy(0.90);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }
}
