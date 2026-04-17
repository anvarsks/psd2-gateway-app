package com.example.psd2gatewayapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayService.class);

    public String getStatus(String correlationId) {
        LOGGER.info("GET API has been hit. X-Correlation-Id={}", correlationId);
        return "psd2-gateway-app is running";
    }
}
