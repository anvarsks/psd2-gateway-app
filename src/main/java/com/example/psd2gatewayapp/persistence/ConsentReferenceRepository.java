package com.example.psd2gatewayapp.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ConsentReferenceRepository {

    private static final RowMapper<ConsentReference> ROW_MAPPER = new ConsentReferenceRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public ConsentReferenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ConsentReference consentReference) {
        jdbcTemplate.update(
                """
                INSERT INTO consent_reference (
                    gateway_consent_id,
                    provider,
                    aspsp_consent_id,
                    psu_id_hash,
                    consent_status,
                    redirect_uri,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                consentReference.gatewayConsentId(),
                consentReference.provider(),
                consentReference.aspspConsentId(),
                consentReference.psuIdHash(),
                consentReference.consentStatus(),
                consentReference.redirectUri(),
                consentReference.createdAt(),
                consentReference.updatedAt());
    }

    public Optional<ConsentReference> findByGatewayConsentId(String gatewayConsentId) {
        return jdbcTemplate.query(
                        "SELECT * FROM consent_reference WHERE gateway_consent_id = ?",
                        ROW_MAPPER,
                        gatewayConsentId)
                .stream()
                .findFirst();
    }

    public void updateStatus(String gatewayConsentId, String consentStatus) {
        jdbcTemplate.update(
                """
                UPDATE consent_reference
                SET consent_status = ?, updated_at = ?
                WHERE gateway_consent_id = ?
                """,
                consentStatus,
                OffsetDateTime.now(),
                gatewayConsentId);
    }

    private static final class ConsentReferenceRowMapper implements RowMapper<ConsentReference> {

        @Override
        public ConsentReference mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConsentReference(
                    rs.getString("gateway_consent_id"),
                    rs.getString("provider"),
                    rs.getString("aspsp_consent_id"),
                    rs.getString("psu_id_hash"),
                    rs.getString("consent_status"),
                    rs.getString("redirect_uri"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class));
        }
    }
}
