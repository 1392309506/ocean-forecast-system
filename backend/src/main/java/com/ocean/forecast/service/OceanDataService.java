package com.ocean.forecast.service;

import com.ocean.forecast.entity.ForecastResult;
import com.ocean.forecast.entity.GridData;
import com.ocean.forecast.entity.OceanData;
import com.ocean.forecast.entity.TimeSeriesData;

public interface OceanDataService {

    GridData getGridData(String dataType, String timestamp);

    TimeSeriesData getTimeSeriesData(Double longitude, Double latitude, String dataType);

    ForecastResult getForecastResult(String region, Integer forecastHours);

    OceanData getObservationData(Double longitude, Double latitude);
}
