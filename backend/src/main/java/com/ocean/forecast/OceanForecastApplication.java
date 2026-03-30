package com.ocean.forecast;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ocean.forecast.mapper")
public class OceanForecastApplication {

    public static void main(String[] args) {
        SpringApplication.run(OceanForecastApplication.class, args);
    }
}
