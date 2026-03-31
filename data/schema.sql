DROP TABLE IF EXISTS ocean_grid_data;
DROP TABLE IF EXISTS ocean_timeseries_data;
DROP TABLE IF EXISTS ocean_forecast_meta;
DROP TABLE IF EXISTS ocean_observation;

CREATE TABLE ocean_grid_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    data_type VARCHAR(64) NOT NULL,
    data_value DOUBLE NOT NULL,
    unit VARCHAR(16) NOT NULL,
    data_time TIMESTAMP NOT NULL
);

CREATE TABLE ocean_timeseries_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    data_type VARCHAR(64) NOT NULL,
    data_value DOUBLE NOT NULL,
    unit VARCHAR(16) NOT NULL,
    data_time TIMESTAMP NOT NULL
);

CREATE TABLE ocean_forecast_meta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    forecast_type VARCHAR(64) NOT NULL,
    region VARCHAR(64) NOT NULL,
    forecast_hours INT NOT NULL,
    confidence_lower DOUBLE NOT NULL,
    confidence_upper DOUBLE NOT NULL,
    accuracy DOUBLE NOT NULL,
    create_time TIMESTAMP NOT NULL
);

CREATE TABLE ocean_observation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    sea_surface_temp DOUBLE NOT NULL,
    salinity DOUBLE NOT NULL,
    current_speed DOUBLE NOT NULL,
    current_direction DOUBLE NOT NULL,
    wave_height DOUBLE NOT NULL,
    wave_period DOUBLE NOT NULL,
    wind_speed DOUBLE NOT NULL,
    wind_direction DOUBLE NOT NULL,
    data_time TIMESTAMP NOT NULL,
    data_source VARCHAR(64) NOT NULL,
    quality_flag INT NOT NULL
);
