package com.example.psd2gatewayapp.service;

import com.example.psd2gatewayapp.persistence.ConsentReference;
import com.example.psd2gatewayapp.persistence.ConsentReferenceRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class GatewayConsentService {

    private final ConsentReferenceRepository consentReferenceRepository;

    public GatewayConsentService(ConsentReferenceRepository consentReferenceRepository) {
        this.consentReferenceRepository = consentReferenceRepository;
    }

    public ConsentReference createConsentReference(
            String provider,
            String aspspConsentId,
            String psuId,
            String redirectUri,
            String consentStatus) {
        OffsetDateTime now = OffsetDateTime.now();
        ConsentReference consentReference = new ConsentReference(
                generateGatewayConsentId(),
                provider,
                aspspConsentId,
                hashPsuId(psuId),
                consentStatus,
                redirectUri,
                now,
                now);
        consentReferenceRepository.save(consentReference);
        return consentReference;
    }

    public ConsentReference getConsentReference(String gatewayConsentId) {
        return consentReferenceRepository.findByGatewayConsentId(gatewayConsentId)
                .orElseThrow(() -> consentNotFound(gatewayConsentId));
    }

    public ConsentReference getConsentReference(String gatewayConsentId, String provider, String psuId) {
        ConsentReference consentReference = getConsentReference(gatewayConsentId);
        if (!provider.equals(consentReference.provider()) || !hashPsuId(psuId).equals(consentReference.psuIdHash())) {
            throw consentNotFound(gatewayConsentId);
        }
        return consentReference;
    }

    public void updateStatus(String gatewayConsentId, String consentStatus) {
        consentReferenceRepository.updateStatus(gatewayConsentId, consentStatus);
    }

    private String generateGatewayConsentId() {
        return "GW-CONSENT-" + UUID.randomUUID();
    }

    private String hashPsuId(String psuId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(psuId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 hashing is not available", exception);
        }
    }

    private ResponseStatusException consentNotFound(String gatewayConsentId) {
        return new ResponseStatusException(NOT_FOUND, "Gateway consent not found: " + gatewayConsentId);
    }
}
