package com.ocean.forecast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OceanData {

    private Long id;
    private Double longitude;
    private Double latitude;
    private Double seaSurfaceTemp;
    private Double salinity;
    private Double currentSpeed;
    private Double currentDirection;
    private Double waveHeight;
    private Double wavePeriod;
    private Double windSpeed;
    private Double windDirection;
    private LocalDateTime dataTime;
    private String dataSource;
    private Integer qualityFlag;
}
