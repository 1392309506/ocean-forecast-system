package com.ocean.forecast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridData {

    private double[] lon;
    private double[] lat;
    private double[][] values;
    private String dataType;
    private String unit;
    private Double minValue;
    private Double maxValue;
    private String timestamp;
}
