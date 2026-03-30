package com.ocean.forecast.controller;

import com.ocean.forecast.entity.ApiResponse;
import com.ocean.forecast.entity.ForecastResult;
import com.ocean.forecast.entity.GridData;
import com.ocean.forecast.entity.OceanData;
import com.ocean.forecast.entity.TimeSeriesData;
import com.ocean.forecast.service.OceanDataService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ocean")
@CrossOrigin(origins = "*")
public class OceanDataController {

    private final OceanDataService oceanDataService;

    public OceanDataController(OceanDataService oceanDataService) {
        this.oceanDataService = oceanDataService;
    }

    @GetMapping("/grid")
    public ApiResponse<GridData> getGridData(
            @RequestParam(defaultValue = "temperature") String dataType,
            @RequestParam(required = false) String timestamp) {
        return ApiResponse.success(oceanDataService.getGridData(dataType, timestamp));
    }

    @GetMapping("/timeseries")
    public ApiResponse<TimeSeriesData> getTimeSeriesData(
            @RequestParam @NotNull Double longitude,
            @RequestParam @NotNull Double latitude,
            @RequestParam(defaultValue = "temperature") String dataType) {
        return ApiResponse.success(oceanDataService.getTimeSeriesData(longitude, latitude, dataType));
    }

    @GetMapping("/forecast")
    public ApiResponse<ForecastResult> getForecastResult(
            @RequestParam(defaultValue = "东海") String region,
            @RequestParam(defaultValue = "72") Integer forecastHours) {
        return ApiResponse.success(oceanDataService.getForecastResult(region, forecastHours));
    }

    @GetMapping("/observation")
    public ApiResponse<OceanData> getObservationData(
            @RequestParam @NotNull Double longitude,
            @RequestParam @NotNull Double latitude) {
        return ApiResponse.success(oceanDataService.getObservationData(longitude, latitude));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Ocean Forecast Backend is running");
    }
}
