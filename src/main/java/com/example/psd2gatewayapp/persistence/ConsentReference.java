package com.example.psd2gatewayapp.persistence;

import java.time.OffsetDateTime;

public record ConsentReference(
        String gatewayConsentId,
        String provider,
        String aspspConsentId,
        String psuIdHash,
        String consentStatus,
        String redirectUri,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
