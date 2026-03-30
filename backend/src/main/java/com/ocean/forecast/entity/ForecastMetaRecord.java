package com.ocean.forecast.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForecastMetaRecord {

    private String forecastType;
    private String region;
    private Integer forecastHours;
    private Double confidenceLower;
    private Double confidenceUpper;
    private Double accuracy;
    private LocalDateTime createTime;
}
