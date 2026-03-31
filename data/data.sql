INSERT INTO ocean_grid_data (longitude, latitude, data_type, data_value, unit, data_time) VALUES
(120.0, 30.0, 'temperature', 21.4, 'degC', CURRENT_TIMESTAMP),
(120.5, 30.0, 'temperature', 21.8, 'degC', CURRENT_TIMESTAMP),
(121.0, 30.0, 'temperature', 22.1, 'degC', CURRENT_TIMESTAMP),
(120.0, 30.5, 'temperature', 21.6, 'degC', CURRENT_TIMESTAMP),
(120.5, 30.5, 'temperature', 22.0, 'degC', CURRENT_TIMESTAMP),
(121.0, 30.5, 'temperature', 22.3, 'degC', CURRENT_TIMESTAMP),
(120.0, 31.0, 'temperature', 21.9, 'degC', CURRENT_TIMESTAMP),
(120.5, 31.0, 'temperature', 22.2, 'degC', CURRENT_TIMESTAMP),
(121.0, 31.0, 'temperature', 22.5, 'degC', CURRENT_TIMESTAMP);

INSERT INTO ocean_timeseries_data (longitude, latitude, data_type, data_value, unit, data_time) VALUES
(120.5, 30.5, 'temperature', 21.1, 'degC', DATEADD('HOUR', -5, CURRENT_TIMESTAMP)),
(120.5, 30.5, 'temperature', 21.3, 'degC', DATEADD('HOUR', -4, CURRENT_TIMESTAMP)),
(120.5, 30.5, 'temperature', 21.6, 'degC', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
(120.5, 30.5, 'temperature', 21.9, 'degC', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
(120.5, 30.5, 'temperature', 22.1, 'degC', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
(120.5, 30.5, 'temperature', 22.0, 'degC', CURRENT_TIMESTAMP);

INSERT INTO ocean_forecast_meta (forecast_type, region, forecast_hours, confidence_lower, confidence_upper, accuracy, create_time) VALUES
('海温预报', '东海', 72, 0.85, 0.95, 0.92, CURRENT_TIMESTAMP);

INSERT INTO ocean_observation (longitude, latitude, sea_surface_temp, salinity, current_speed, current_direction,
wave_height, wave_period, wind_speed, wind_direction, data_time, data_source, quality_flag) VALUES
(120.5, 30.5, 22.0, 34.5, 0.42, 135.0, 1.6, 7.2, 6.8, 118.0, CURRENT_TIMESTAMP, 'SIM_DB', 1);
