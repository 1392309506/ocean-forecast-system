package com.ocean.forecast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResult {

    private String forecastId;
    private String forecastType;
    private String region;
    private Integer forecastHours;
    private List<String> forecastTimes;
    private List<GridData> gridDataList;
    private Double confidenceLower;
    private Double confidenceUpper;
    private Double accuracy;
    private String createTime;
}
