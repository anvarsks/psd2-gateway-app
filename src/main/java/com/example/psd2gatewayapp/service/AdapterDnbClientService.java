package com.example.psd2gatewayapp.service;

import com.example.psd2gatewayapp.config.InternalAdapterProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdapterDnbClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterDnbClientService.class);
    private final InternalAdapterProperties properties;
    private final ObjectMapper objectMapper;

    public AdapterDnbClientService(InternalAdapterProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> fetchAccountSummary(String correlationId)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest("GET", properties.getAccountSummaryPath(), correlationId, null, null);
    }

    public Map<String, Object> createConsent(
            String correlationId,
            String psuId,
            String redirectUri,
            Map<String, Object> requestBody)
            throws IOException, InterruptedException, GeneralSecurityException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("PSU-ID", psuId);
        headers.put("TPP-Redirect-URI", redirectUri);
        return sendRequest("POST", properties.getConsentsPath(), correlationId, headers, requestBody);
    }

    public Map<String, Object> getConsent(String correlationId, String psuId, String consentId)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "GET",
                properties.getConsentsPath() + "/" + consentId,
                correlationId,
                Map.of("PSU-ID", psuId),
                null);
    }

    public Map<String, Object> deleteConsent(String correlationId, String psuId, String consentId)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "DELETE",
                properties.getConsentsPath() + "/" + consentId,
                correlationId,
                Map.of("PSU-ID", psuId),
                null);
    }

    public Map<String, Object> getConsentStatus(String correlationId, String psuId, String consentId)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "GET",
                properties.getConsentsPath() + "/" + consentId + "/status",
                correlationId,
                Map.of("PSU-ID", psuId),
                null);
    }

    public Map<String, Object> fetchAccounts(String correlationId, String consentId)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "GET",
                properties.getAccountsPath(),
                correlationId,
                Map.of("Consent-ID", consentId),
                null);
    }

    public Map<String, Object> fetchAccountDetails(String correlationId, String consentId, String accountNumber)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "GET",
                properties.getAccountsPath() + "/" + accountNumber,
                correlationId,
                Map.of("Consent-ID", consentId),
                null);
    }

    public Map<String, Object> fetchBalances(String correlationId, String consentId, String accountNumber)
            throws IOException, InterruptedException, GeneralSecurityException {
        return sendRequest(
                "GET",
                properties.getAccountsPath() + "/" + accountNumber + "/balances",
                correlationId,
                Map.of("Consent-ID", consentId),
                null);
    }

    private Map<String, Object> sendRequest(
            String method,
            String path,
            String correlationId,
            Map<String, String> extraHeaders,
            Map<String, Object> requestBody)
            throws IOException, InterruptedException, GeneralSecurityException {
        HttpClient client = HttpClient.newBuilder()
                .sslContext(buildSslContext())
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + path))
                .timeout(Duration.ofSeconds(10))
                .header("X-Correlation-Id", correlationId == null ? "n/a" : correlationId);

        Map<String, String> headers = extraHeaders == null ? Collections.emptyMap() : extraHeaders;
        headers.forEach(requestBuilder::header);

        if ("POST".equalsIgnoreCase(method)) {
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            requestBuilder.DELETE();
        } else {
            requestBuilder.GET();
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        LOGGER.info("Adapter-DNB call completed. method={} path={} upstreamStatus={} X-Correlation-Id={}",
                method,
                path,
                response.statusCode(),
                response.headers().firstValue("X-Correlation-Id").orElse(correlationId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statusCode", response.statusCode());
        result.put("correlationId", response.headers().firstValue("X-Correlation-Id").orElse(correlationId));
        result.put("data", parseBody(response.body()));
        return result;
    }

    private Object parseBody(String body) throws IOException {
        if (body == null || body.isBlank()) {
            return Collections.emptyMap();
        }
        return objectMapper.readValue(body, new TypeReference<Object>() { });
    }

    private SSLContext buildSslContext() throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream inputStream = new FileInputStream(properties.getKeyStorePath())) {
            keyStore.load(inputStream, properties.getKeyStorePassword().toCharArray());
        }

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream inputStream = new FileInputStream(properties.getTrustStorePath())) {
            trustStore.load(inputStream, properties.getTrustStorePassword().toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, properties.getKeyStorePassword().toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }
}
