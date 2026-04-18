package com.example.tppclientapp.service;

import com.example.tppclientapp.config.TppGatewayProperties;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TppGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TppGatewayService.class);

    private final TppGatewayProperties properties;

    public TppGatewayService(TppGatewayProperties properties) {
        this.properties = properties;
    }

    public Map<String, Object> fetchGatewayStatus() throws IOException, InterruptedException, GeneralSecurityException {
        HttpClient client = HttpClient.newBuilder()
                .sslContext(buildSslContext())
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + properties.getStatusPath()))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String correlationId = response.headers().firstValue("X-Correlation-Id").orElse("n/a");

        LOGGER.info("TPP call completed. upstreamStatus={} X-Correlation-Id={}", response.statusCode(), correlationId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("upstreamStatus", response.statusCode());
        result.put("correlationId", correlationId);
        result.put("body", response.body());
        result.put("gatewayUrl", properties.getBaseUrl() + properties.getStatusPath());
        return result;
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
