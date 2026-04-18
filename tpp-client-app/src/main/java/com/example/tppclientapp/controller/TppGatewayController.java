package com.example.tppclientapp.controller;

import com.example.tppclientapp.service.TppGatewayService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tpp")
public class TppGatewayController {

    private final TppGatewayService tppGatewayService;

    public TppGatewayController(TppGatewayService tppGatewayService) {
        this.tppGatewayService = tppGatewayService;
    }

    @GetMapping("/gateway-status")
    public Map<String, Object> gatewayStatus() throws IOException, GeneralSecurityException, InterruptedException {
        return tppGatewayService.fetchGatewayStatus();
    }
}
