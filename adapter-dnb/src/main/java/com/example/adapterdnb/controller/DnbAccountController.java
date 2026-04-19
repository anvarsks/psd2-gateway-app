package com.example.adapterdnb.controller;

import com.example.adapterdnb.model.DnbAccountSummary;
import com.example.adapterdnb.service.DnbAccountService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/dnb")
public class DnbAccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnbAccountController.class);
    private final DnbAccountService dnbAccountService;

    public DnbAccountController(DnbAccountService dnbAccountService) {
        this.dnbAccountService = dnbAccountService;
    }

    @GetMapping("/accounts/summary")
    public DnbAccountSummary getSummary(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received account summary request. X-Correlation-Id={}", correlationId);
        return dnbAccountService.getAccountSummary(correlationId);
    }

    @PostMapping("/consents")
    public Map<String, Object> createConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @RequestHeader("TPP-Redirect-URI") String redirectUri,
            @RequestBody Map<String, Object> requestBody)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received create consent request. PSU-ID={} X-Correlation-Id={}", psuId, correlationId);
        return dnbAccountService.createConsent(correlationId, psuId, redirectUri, requestBody);
    }

    @GetMapping("/consents/{consentId}")
    public Map<String, Object> getConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received get consent request. consentId={} X-Correlation-Id={}", consentId, correlationId);
        return dnbAccountService.getConsent(correlationId, psuId, consentId);
    }

    @DeleteMapping("/consents/{consentId}")
    public Map<String, Object> deleteConsent(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received delete consent request. consentId={} X-Correlation-Id={}", consentId, correlationId);
        return dnbAccountService.deleteConsent(correlationId, psuId, consentId);
    }

    @GetMapping("/consents/{consentId}/status")
    public Map<String, Object> getConsentStatus(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received get consent status request. consentId={} X-Correlation-Id={}", consentId, correlationId);
        return dnbAccountService.getConsentStatus(correlationId, psuId, consentId);
    }

    @GetMapping("/accounts")
    public Map<String, Object> getAccounts(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received get accounts request. consentId={} X-Correlation-Id={}", consentId, correlationId);
        return dnbAccountService.getAccounts(correlationId, consentId);
    }

    @GetMapping("/accounts/{accountNumber}")
    public Map<String, Object> getAccountDetails(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received get account details request. accountNumber={} X-Correlation-Id={}", accountNumber, correlationId);
        return dnbAccountService.getAccountDetails(correlationId, consentId, accountNumber);
    }

    @GetMapping("/accounts/{accountNumber}/balances")
    public Map<String, Object> getBalances(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Adapter-DNB received get balances request. accountNumber={} X-Correlation-Id={}", accountNumber, correlationId);
        return dnbAccountService.getBalances(correlationId, consentId, accountNumber);
    }
}
