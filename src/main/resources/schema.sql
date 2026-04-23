CREATE TABLE IF NOT EXISTS consent_reference (
    gateway_consent_id VARCHAR(80) PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    aspsp_consent_id VARCHAR(128) NOT NULL,
    psu_id_hash VARCHAR(64) NOT NULL,
    consent_status VARCHAR(64) NOT NULL,
    redirect_uri VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_consent_reference_provider_aspsp
    ON consent_reference (provider, aspsp_consent_id);

CREATE INDEX IF NOT EXISTS ix_consent_reference_psu_hash
    ON consent_reference (psu_id_hash);
