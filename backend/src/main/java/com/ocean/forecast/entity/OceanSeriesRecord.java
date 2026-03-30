package com.ocean.forecast.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OceanSeriesRecord {

    private Double longitude;
    private Double latitude;
    private String dataType;
    private Double value;
    private String unit;
    private LocalDateTime dataTime;
}
