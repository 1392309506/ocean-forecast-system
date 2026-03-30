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
public class TimeSeriesData {

    private List<String> timestamps;
    private List<Double> values;
    private String dataType;
    private String unit;
    private String locationName;
    private Double longitude;
    private Double latitude;
}
