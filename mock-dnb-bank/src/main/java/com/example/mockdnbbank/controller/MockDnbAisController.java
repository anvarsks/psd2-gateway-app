package com.example.mockdnbbank.controller;

import com.example.mockdnbbank.service.MockDnbBankService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class MockDnbAisController {

    private final MockDnbBankService mockDnbBankService;

    public MockDnbAisController(MockDnbBankService mockDnbBankService) {
        this.mockDnbBankService = mockDnbBankService;
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createConsent(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("TPP-Redirect-URI") String redirectUri,
            @RequestHeader("PSU-ID") String psuId,
            @RequestBody Map<String, Object> requestBody) {
        return mockDnbBankService.createConsent(requestBody);
    }

    @GetMapping("/consents/{consentId}")
    public Map<String, Object> getConsent(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId) {
        return mockDnbBankService.getConsent(consentId);
    }

    @DeleteMapping("/consents/{consentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsent(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId) {
        mockDnbBankService.deleteConsent(consentId);
    }

    @GetMapping("/consents/{consentId}/status")
    public Map<String, Object> getConsentStatus(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("PSU-ID") String psuId,
            @PathVariable String consentId) {
        return mockDnbBankService.getConsentStatus(consentId);
    }

    @GetMapping("/accounts")
    public Map<String, Object> getAccounts(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("Consent-ID") String consentId) {
        return mockDnbBankService.getAccounts(consentId);
    }

    @GetMapping("/accounts/{accountNumber}")
    public Map<String, Object> getAccountDetails(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber) {
        return mockDnbBankService.getAccountDetails(consentId, accountNumber);
    }

    @GetMapping("/accounts/{accountNumber}/balances")
    public Map<String, Object> getBalances(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("Consent-ID") String consentId,
            @PathVariable String accountNumber) {
        return mockDnbBankService.getBalances(consentId, accountNumber);
    }
}
