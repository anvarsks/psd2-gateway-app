package com.example.psd2gatewayapp.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayService.class);
    private static final String DNB_PROVIDER = "dnb";
    private final AdapterDnbClientService adapterDnbClientService;
    private final GatewayConsentService gatewayConsentService;

    public GatewayService(
            AdapterDnbClientService adapterDnbClientService,
            GatewayConsentService gatewayConsentService) {
        this.adapterDnbClientService = adapterDnbClientService;
        this.gatewayConsentService = gatewayConsentService;
    }

    public String getStatus(String correlationId) {
        LOGGER.info("GET API has been hit. X-Correlation-Id={}", correlationId);
        return "psd2-gateway-app is running";
    }

    public Map<String, Object> getDnbAccountSummary(String correlationId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB account summary through adapter. X-Correlation-Id={}", correlationId);

        Map<String, Object> adapterResponse = adapterDnbClientService.fetchAccountSummary(correlationId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", adapterResponse.get("data"));
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> createDnbConsent(
            String correlationId,
            String psuId,
            String redirectUri,
            Map<String, Object> requestBody)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Creating DNB consent through adapter. X-Correlation-Id={}", correlationId);
        Map<String, Object> adapterResponse =
                adapterDnbClientService.createConsent(correlationId, psuId, redirectUri, requestBody);
        Map<String, Object> rawConsent = asMap(adapterResponse.get("data"));
        String aspspConsentId = String.valueOf(rawConsent.get("consentId"));
        String consentStatus = String.valueOf(rawConsent.get("consentStatus"));
        var consentReference = gatewayConsentService.createConsentReference(
                DNB_PROVIDER,
                aspspConsentId,
                psuId,
                redirectUri,
                consentStatus);

        Map<String, Object> consent = new LinkedHashMap<>();
        consent.put("id", consentReference.gatewayConsentId());
        consent.put("status", consentStatus);
        consent.put("links", buildGatewayConsentLinks(consentReference.gatewayConsentId()));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("consent", consent);
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> getDnbConsent(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB consent through adapter. consentId={} X-Correlation-Id={}", consentId, correlationId);
        var consentReference = gatewayConsentService.getConsentReference(consentId, DNB_PROVIDER, psuId);
        Map<String, Object> adapterResponse =
                adapterDnbClientService.getConsent(correlationId, psuId, consentReference.aspspConsentId());
        Map<String, Object> rawConsent = asMap(adapterResponse.get("data"));
        gatewayConsentService.updateStatus(consentId, String.valueOf(rawConsent.get("consentStatus")));

        Map<String, Object> consent = new LinkedHashMap<>();
        consent.put("id", consentId);
        consent.put("status", rawConsent.get("consentStatus"));
        consent.put("access", rawConsent.get("access"));
        consent.put("validUntil", rawConsent.get("validUntil"));
        consent.put("frequencyPerDay", rawConsent.get("frequencyPerDay"));
        consent.put("recurringIndicator", rawConsent.get("recurringIndicator"));
        consent.put("links", buildGatewayConsentLinks(consentId));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("consent", consent);
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> deleteDnbConsent(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Deleting DNB consent through adapter. consentId={} X-Correlation-Id={}", consentId, correlationId);
        var consentReference = gatewayConsentService.getConsentReference(consentId, DNB_PROVIDER, psuId);
        Map<String, Object> adapterResponse =
                adapterDnbClientService.deleteConsent(correlationId, psuId, consentReference.aspspConsentId());
        gatewayConsentService.updateStatus(consentId, "terminatedByTpp");
        Map<String, Object> consent = new LinkedHashMap<>();
        consent.put("id", consentId);
        consent.put("status", "terminatedByTpp");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("consent", consent);
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> getDnbConsentStatus(String correlationId, String psuId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB consent status through adapter. consentId={} X-Correlation-Id={}", consentId, correlationId);
        var consentReference = gatewayConsentService.getConsentReference(consentId, DNB_PROVIDER, psuId);
        Map<String, Object> adapterResponse =
                adapterDnbClientService.getConsentStatus(correlationId, psuId, consentReference.aspspConsentId());
        Map<String, Object> rawStatus = asMap(adapterResponse.get("data"));
        gatewayConsentService.updateStatus(consentId, String.valueOf(rawStatus.get("consentStatus")));
        Map<String, Object> consent = new LinkedHashMap<>();
        consent.put("id", consentId);
        consent.put("status", rawStatus.get("consentStatus"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("consent", consent);
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> getDnbAccounts(String correlationId, String consentId)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB accounts through adapter. X-Correlation-Id={}", correlationId);
        String aspspConsentId = gatewayConsentService.getConsentReference(consentId).aspspConsentId();
        Map<String, Object> adapterResponse = adapterDnbClientService.fetchAccounts(correlationId, aspspConsentId);
        Map<String, Object> raw = asMap(adapterResponse.get("data"));
        List<Map<String, Object>> accounts = mapAccounts(raw.get("accounts"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accounts", accounts);
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> getDnbAccountDetails(String correlationId, String consentId, String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB account details through adapter. accountNumber={} X-Correlation-Id={}", accountNumber, correlationId);
        String aspspConsentId = gatewayConsentService.getConsentReference(consentId).aspspConsentId();
        Map<String, Object> adapterResponse =
                adapterDnbClientService.fetchAccountDetails(correlationId, aspspConsentId, accountNumber);
        Map<String, Object> raw = asMap(adapterResponse.get("data"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account", mapAccount(raw));
        return buildEnvelope(adapterResponse, payload);
    }

    public Map<String, Object> getDnbBalances(String correlationId, String consentId, String accountNumber)
            throws IOException, GeneralSecurityException, InterruptedException {
        LOGGER.info("Fetching DNB balances through adapter. accountNumber={} X-Correlation-Id={}", accountNumber, correlationId);
        String aspspConsentId = gatewayConsentService.getConsentReference(consentId).aspspConsentId();
        Map<String, Object> adapterResponse =
                adapterDnbClientService.fetchBalances(correlationId, aspspConsentId, accountNumber);
        Map<String, Object> raw = asMap(adapterResponse.get("data"));
        List<Map<String, Object>> balances = mapBalances(raw.get("balances"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accountId", raw.get("accountId"));
        payload.put("balances", balances);
        return buildEnvelope(adapterResponse, payload);
    }

    private Map<String, Object> buildEnvelope(Map<String, Object> adapterResponse, Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", DNB_PROVIDER);
        result.put("adapter", "adapter-dnb");
        result.put("correlationId", adapterResponse.get("correlationId"));
        result.put("status", "SUCCESS");
        result.putAll(payload);
        return result;
    }

    private Map<String, Object> buildGatewayConsentLinks(String gatewayConsentId) {
        return Map.of(
                "self", Map.of("href", "/api/v1/gateway/aspsps/dnb/consents/" + gatewayConsentId),
                "status", Map.of("href", "/api/v1/gateway/aspsps/dnb/consents/" + gatewayConsentId + "/status"));
    }

    private List<Map<String, Object>> mapAccounts(Object rawAccounts) {
        if (!(rawAccounts instanceof List<?> list)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> accounts = new ArrayList<>();
        for (Object rawAccount : list) {
            accounts.add(mapAccount(asMap(rawAccount)));
        }
        return accounts;
    }

    private Map<String, Object> mapAccount(Map<String, Object> rawAccount) {
        Map<String, Object> account = new LinkedHashMap<>();
        account.put("accountId", rawAccount.get("bban") != null ? rawAccount.get("bban") : rawAccount.get("iban"));
        account.put("iban", rawAccount.get("iban"));
        account.put("bban", rawAccount.get("bban"));
        account.put("name", rawAccount.get("name"));
        account.put("ownerName", rawAccount.get("ownerName"));
        account.put("currency", rawAccount.get("currency"));
        account.put("usage", rawAccount.get("usage"));
        account.put("status", rawAccount.get("status"));
        account.put("balances", mapBalances(rawAccount.get("balances")));
        return account;
    }

    private List<Map<String, Object>> mapBalances(Object rawBalances) {
        if (!(rawBalances instanceof List<?> list)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> balances = new ArrayList<>();
        for (Object rawBalance : list) {
            Map<String, Object> balanceMap = asMap(rawBalance);
            Map<String, Object> amountMap = asMap(balanceMap.get("balanceAmount"));

            Map<String, Object> balance = new LinkedHashMap<>();
            balance.put("type", balanceMap.get("balanceType"));
            balance.put("amount", amountMap.get("amount"));
            balance.put("currency", amountMap.get("currency"));
            balance.put("referenceDate", balanceMap.get("referenceDate"));
            balance.put("lastChangeDateTime", balanceMap.get("lastChangeDateTime"));
            balances.add(balance);
        }
        return balances;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }
}
