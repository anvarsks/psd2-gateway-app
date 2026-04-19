package com.example.adapterdnb.service;

import com.example.adapterdnb.model.DnbAccountSummary;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DnbAccountService {

    private final DnbBankClientService dnbBankClientService;

    public DnbAccountService(DnbBankClientService dnbBankClientService) {
        this.dnbBankClientService = dnbBankClientService;
    }

    public DnbAccountSummary getAccountSummary(String correlationId)
            throws IOException, GeneralSecurityException, InterruptedException {
        Map<String, Object> accountsResponse = dnbBankClientService.getAccounts(correlationId, "CONSENT-STATIC-001");
        Object rawAccounts = asMap(accountsResponse.get("data")).get("accounts");
        if (!(rawAccounts instanceof List<?> accounts) || accounts.isEmpty()) {
            return new DnbAccountSummary("dnb", "n/a", "CURRENT", "NOK", "0.00");
        }

        Map<String, Object> firstAccount = asMap(accounts.get(0));
        String accountId = String.valueOf(firstAccount.getOrDefault("bban", firstAccount.get("iban")));
        String currency = String.valueOf(firstAccount.getOrDefault("currency", "NOK"));
        String availableBalance = "0.00";
        Object rawBalances = firstAccount.get("balances");
        if (rawBalances instanceof List<?> balances && !balances.isEmpty()) {
            Map<String, Object> firstBalance = asMap(balances.get(0));
            availableBalance = String.valueOf(asMap(firstBalance.get("balanceAmount")).getOrDefault("amount", "0.00"));
        }

        return new DnbAccountSummary("dnb", accountId, "CURRENT", currency, availableBalance);
    }

    public Map<String, Object> createConsent(
            String correlationId,
            String psuId,
            String redirectUri,
            Map<String, Object> requestBody)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.createConsent(correlationId, psuId, redirectUri, requestBody));
    }

    public Map<String, Object> getConsent(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.getConsent(correlationId, psuId, consentId));
    }

    public Map<String, Object> deleteConsent(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.deleteConsent(correlationId, psuId, consentId));
    }

    public Map<String, Object> getConsentStatus(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.getConsentStatus(correlationId, psuId, consentId));
    }

    public Map<String, Object> getAccounts(String correlationId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.getAccounts(correlationId, consentId));
    }

    public Map<String, Object> getAccountDetails(String correlationId, String consentId, String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.getAccountDetails(correlationId, consentId, accountNumber));
    }

    public Map<String, Object> getBalances(String correlationId, String consentId, String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        return unwrap(dnbBankClientService.getBalances(correlationId, consentId, accountNumber));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrap(Map<String, Object> response) {
        Object data = response.get("data");
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }
}
