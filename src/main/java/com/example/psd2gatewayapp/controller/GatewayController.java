package com.example.psd2gatewayapp.controller;

import com.example.psd2gatewayapp.service.GatewayService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/aspsps/dnb/accounts/summary")
    public Map<String, Object> getDnbAccountSummary(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbAccountSummary(correlationId);
    }

    @PostMapping("/aspsps/dnb/consents")
    public Map<String, Object> createDnbConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @RequestHeader("TPP-Redirect-URI") String redirectUri,
            @RequestBody Map<String, Object> requestBody)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.createDnbConsent(correlationId, psuId, redirectUri, requestBody);
    }

    @GetMapping("/aspsps/dnb/consents/{consentId}")
    public Map<String, Object> getDnbConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbConsent(correlationId, psuId, consentId);
    }

    @DeleteMapping("/aspsps/dnb/consents/{consentId}")
    public Map<String, Object> deleteDnbConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.deleteDnbConsent(correlationId, psuId, consentId);
    }

    @GetMapping("/aspsps/dnb/consents/{consentId}/status")
    public Map<String, Object> getDnbConsentStatus(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbConsentStatus(correlationId, psuId, consentId);
    }

    @GetMapping("/aspsps/dnb/accounts")
    public Map<String, Object> getDnbAccounts(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbAccounts(correlationId, consentId);
    }

    @GetMapping("/aspsps/dnb/accounts/{accountNumber}")
    public Map<String, Object> getDnbAccountDetails(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbAccountDetails(correlationId, consentId, accountNumber);
    }

    @GetMapping("/aspsps/dnb/accounts/{accountNumber}/balances")
    public Map<String, Object> getDnbBalances(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        return gatewayService.getDnbBalances(correlationId, consentId, accountNumber);
    }
}
