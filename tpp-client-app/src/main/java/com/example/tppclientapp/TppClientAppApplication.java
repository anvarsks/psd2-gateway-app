package com.example.tppclientapp;

import com.example.tppclientapp.config.TppGatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TppGatewayProperties.class)
public class TppClientAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TppClientAppApplication.class, args);
    }
}
