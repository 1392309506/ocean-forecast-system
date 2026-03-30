package com.ocean.forecast.mapper;

import com.ocean.forecast.entity.ForecastMetaRecord;
import com.ocean.forecast.entity.OceanGridRecord;
import com.ocean.forecast.entity.OceanData;
import com.ocean.forecast.entity.OceanSeriesRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OceanDataMapper {

        @Select("SELECT longitude, latitude, data_type, data_value AS value, unit, data_time " +
                        "FROM ocean_grid_data " +
                        "WHERE data_type = #{dataType} " +
                        "AND data_time = (SELECT MAX(data_time) FROM ocean_grid_data WHERE data_type = #{dataType}) " +
                        "ORDER BY latitude, longitude")
        List<OceanGridRecord> selectGridRows(@Param("dataType") String dataType);

        @Select("SELECT longitude, latitude, data_type, data_value AS value, unit, data_time " +
                        "FROM ocean_timeseries_data " +
                        "WHERE longitude = #{longitude} AND latitude = #{latitude} AND data_type = #{dataType} " +
                        "ORDER BY data_time DESC LIMIT 24")
        List<OceanSeriesRecord> selectTimeSeriesRows(
                        @Param("longitude") Double longitude,
                        @Param("latitude") Double latitude,
                        @Param("dataType") String dataType);

        @Select("SELECT forecast_type, region, forecast_hours, confidence_lower, confidence_upper, accuracy, create_time "
                        +
                        "FROM ocean_forecast_meta " +
                        "WHERE region = #{region} AND forecast_hours = #{forecastHours} " +
                        "ORDER BY create_time DESC LIMIT 1")
        ForecastMetaRecord selectForecastMeta(
                        @Param("region") String region,
                        @Param("forecastHours") Integer forecastHours);

        @Select("SELECT id, longitude, latitude, sea_surface_temp, salinity, current_speed, current_direction, " +
                        "wave_height, wave_period, wind_speed, wind_direction, data_time, data_source, quality_flag " +
                        "FROM ocean_observation " +
                        "WHERE longitude = #{longitude} AND latitude = #{latitude} " +
                        "ORDER BY data_time DESC LIMIT 1")
        OceanData selectObservationData(@Param("longitude") Double longitude, @Param("latitude") Double latitude);
}
