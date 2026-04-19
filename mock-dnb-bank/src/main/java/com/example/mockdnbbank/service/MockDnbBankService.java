package com.example.mockdnbbank.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MockDnbBankService {

    private final ConcurrentMap<String, Map<String, Object>> consents = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> accountsByNumber = new LinkedHashMap<>();

    public MockDnbBankService() {
        Map<String, Object> account1 = new LinkedHashMap<>();
        account1.put("bban", "15030012345");
        account1.put("iban", "NO0015030012345");
        account1.put("currency", "NOK");
        account1.put("name", "Primary Salary Account");
        account1.put("ownerName", "Demo Customer");
        account1.put("status", "enabled");
        account1.put("usage", "PRIV");
        account1.put("balances", List.of(balance("interimAvailable", "125000.55", "NOK", "2026-04-19")));
        accountsByNumber.put("15030012345", account1);

        Map<String, Object> account2 = new LinkedHashMap<>();
        account2.put("bban", "15030067890");
        account2.put("iban", "NO0015030067890");
        account2.put("currency", "NOK");
        account2.put("name", "Emergency Savings");
        account2.put("ownerName", "Demo Customer");
        account2.put("status", "enabled");
        account2.put("usage", "PRIV");
        account2.put("balances", List.of(balance("closingBooked", "45800.10", "NOK", "2026-04-19")));
        accountsByNumber.put("15030067890", account2);

        Map<String, Object> staticConsent = new LinkedHashMap<>();
        staticConsent.put("consentId", "CONSENT-STATIC-001");
        staticConsent.put("consentStatus", "valid");
        staticConsent.put("frequencyPerDay", 4);
        staticConsent.put("recurringIndicator", true);
        staticConsent.put("validUntil", "2026-12-31");
        staticConsent.put("lastActionDate", OffsetDateTime.now().toString());
        staticConsent.put("access", defaultAccess());
        staticConsent.put("_links", consentLinks("CONSENT-STATIC-001"));
        consents.put("CONSENT-STATIC-001", staticConsent);
    }

    public Map<String, Object> createConsent(Map<String, Object> requestBody) {
        String consentId = "CONSENT-" + (consents.size() + 1);
        Map<String, Object> consent = new LinkedHashMap<>();
        consent.put("consentId", consentId);
        consent.put("consentStatus", "received");
        consent.put("frequencyPerDay", requestBody.getOrDefault("frequencyPerDay", 4));
        consent.put("recurringIndicator", requestBody.getOrDefault("recurringIndicator", true));
        consent.put("validUntil", requestBody.getOrDefault("validUntil", "2026-12-31"));
        consent.put("lastActionDate", OffsetDateTime.now().toString());
        consent.put("access", requestBody.getOrDefault("access", defaultAccess()));
        consent.put("_links", consentLinks(consentId));
        consents.put(consentId, consent);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("consentId", consentId);
        response.put("consentStatus", "received");
        response.put("_links", consentLinks(consentId));
        return response;
    }

    public Map<String, Object> getConsent(String consentId) {
        return new LinkedHashMap<>(requireConsent(consentId));
    }

    public void deleteConsent(String consentId) {
        Map<String, Object> consent = requireConsent(consentId);
        consent.put("consentStatus", "terminatedByTpp");
        consent.put("lastActionDate", OffsetDateTime.now().toString());
    }

    public Map<String, Object> getConsentStatus(String consentId) {
        Map<String, Object> consent = requireConsent(consentId);
        return Map.of("consentStatus", consent.get("consentStatus"));
    }

    public Map<String, Object> getAccounts(String consentId) {
        requireConsent(consentId);
        return Map.of("accounts", new ArrayList<>(accountsByNumber.values()));
    }

    public Map<String, Object> getAccountDetails(String consentId, String accountNumber) {
        requireConsent(consentId);
        return new LinkedHashMap<>(requireAccount(accountNumber));
    }

    public Map<String, Object> getBalances(String consentId, String accountNumber) {
        requireConsent(consentId);
        Map<String, Object> account = requireAccount(accountNumber);
        return Map.of(
                "accountId", accountNumber,
                "balances", account.get("balances"));
    }

    private Map<String, Object> requireConsent(String consentId) {
        Map<String, Object> consent = consents.get(consentId);
        if (consent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consent not found");
        }
        return consent;
    }

    private Map<String, Object> requireAccount(String accountNumber) {
        Map<String, Object> account = accountsByNumber.get(accountNumber);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }

    private Map<String, Object> balance(String type, String amount, String currency, String referenceDate) {
        Map<String, Object> amountMap = new LinkedHashMap<>();
        amountMap.put("amount", amount);
        amountMap.put("currency", currency);

        Map<String, Object> balance = new LinkedHashMap<>();
        balance.put("balanceAmount", amountMap);
        balance.put("balanceType", type);
        balance.put("referenceDate", referenceDate);
        balance.put("lastChangeDateTime", OffsetDateTime.now().toString());
        return balance;
    }

    private Map<String, Object> defaultAccess() {
        List<Map<String, Object>> accountRefs = new ArrayList<>();
        accountRefs.add(Map.of("bban", "15030012345", "iban", "NO0015030012345"));
        accountRefs.add(Map.of("bban", "15030067890", "iban", "NO0015030067890"));
        return Map.of(
                "accounts", accountRefs,
                "balances", accountRefs,
                "transactions", accountRefs);
    }

    private Map<String, Object> consentLinks(String consentId) {
        return Map.of(
                "self", Map.of("href", "/v1/consents/" + consentId),
                "status", Map.of("href", "/v1/consents/" + consentId + "/status"));
    }
}
