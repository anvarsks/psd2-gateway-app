package com.example.adapterdnb.model;

public class DnbAccountSummary {

    private String aspsp;
    private String accountId;
    private String accountType;
    private String currency;
    private String availableBalance;

    public DnbAccountSummary() {
    }

    public DnbAccountSummary(String aspsp, String accountId, String accountType, String currency, String availableBalance) {
        this.aspsp = aspsp;
        this.accountId = accountId;
        this.accountType = accountType;
        this.currency = currency;
        this.availableBalance = availableBalance;
    }

    public String getAspsp() {
        return aspsp;
    }

    public void setAspsp(String aspsp) {
        this.aspsp = aspsp;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }
}
