package com.example.psd2gatewayapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.adapter.dnb")
public class InternalAdapterProperties {

    private String baseUrl;
    private String accountSummaryPath;
    private String consentsPath;
    private String accountsPath;
    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAccountSummaryPath() {
        return accountSummaryPath;
    }

    public void setAccountSummaryPath(String accountSummaryPath) {
        this.accountSummaryPath = accountSummaryPath;
    }

    public String getConsentsPath() {
        return consentsPath;
    }

    public void setConsentsPath(String consentsPath) {
        this.consentsPath = consentsPath;
    }

    public String getAccountsPath() {
        return accountsPath;
    }

    public void setAccountsPath(String accountsPath) {
        this.accountsPath = accountsPath;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
