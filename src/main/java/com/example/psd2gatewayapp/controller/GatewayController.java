package com.example.psd2gatewayapp.controller;

import com.example.psd2gatewayapp.service.GatewayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/status")
    public String getStatus(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return gatewayService.getStatus(correlationId);
    }
}
