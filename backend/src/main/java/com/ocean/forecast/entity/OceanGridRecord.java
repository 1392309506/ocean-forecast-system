package com.ocean.forecast.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OceanGridRecord {

    private Double longitude;
    private Double latitude;
    private String dataType;
    private Double value;
    private String unit;
    private LocalDateTime dataTime;
}
