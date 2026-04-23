package com.example.psd2gatewayapp;

import com.example.psd2gatewayapp.persistence.ConsentReference;
import com.example.psd2gatewayapp.persistence.ConsentReferenceRepository;
import com.example.psd2gatewayapp.service.AdapterDnbClientService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayConsentPersistenceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConsentReferenceRepository consentReferenceRepository;

    @MockBean
    private AdapterDnbClientService adapterDnbClientService;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM consent_reference");
    }

    @Test
    void createConsentReturnsGatewayIdAndPersistsMapping() throws Exception {
        when(adapterDnbClientService.createConsent(eq("corr-1"), eq("demo-psu"), eq("https://tpp.local/callback"), eq(Map.of(
                "access", Map.of("accounts", List.of(Map.of("iban", "NO0015030012345"))),
                "recurringIndicator", true,
                "validUntil", "2026-12-31",
                "frequencyPerDay", 4))))
                .thenReturn(Map.of(
                        "correlationId", "corr-1",
                        "data", Map.of(
                                "consentId", "CONSENT-42",
                                "consentStatus", "received",
                                "_links", Map.of())));

        mockMvc.perform(post("/api/v1/gateway/aspsps/dnb/consents")
                        .header("X-Correlation-Id", "corr-1")
                        .header("PSU-ID", "demo-psu")
                        .header("TPP-Redirect-URI", "https://tpp.local/callback")
                        .contentType("application/json")
                        .content("""
                                {
                                  "access": {
                                    "accounts": [
                                      { "iban": "NO0015030012345" }
                                    ]
                                  },
                                  "recurringIndicator": true,
                                  "validUntil": "2026-12-31",
                                  "frequencyPerDay": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consent.id").value(org.hamcrest.Matchers.startsWith("GW-CONSENT-")))
                .andExpect(jsonPath("$.consent.status").value("received"))
                .andExpect(jsonPath("$.consent.links.self.href").exists())
                .andExpect(jsonPath("$.consent.id").value(org.hamcrest.Matchers.not("CONSENT-42")));

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM consent_reference");
        assertThat(row.get("provider")).isEqualTo("dnb");
        assertThat(row.get("aspsp_consent_id")).isEqualTo("CONSENT-42");
        assertThat(row.get("psu_id_hash")).isNotEqualTo("demo-psu");
        assertThat(row.get("consent_status")).isEqualTo("received");
    }

    @Test
    void accountLookupTranslatesGatewayConsentIdToAspspConsentId() throws Exception {
        consentReferenceRepository.save(new ConsentReference(
                "GW-CONSENT-TEST-001",
                "dnb",
                "CONSENT-STATIC-001",
                "unused-for-account-lookups",
                "valid",
                "https://tpp.local/callback",
                OffsetDateTime.now(),
                OffsetDateTime.now()));

        when(adapterDnbClientService.fetchAccounts("corr-2", "CONSENT-STATIC-001"))
                .thenReturn(Map.of(
                        "correlationId", "corr-2",
                        "data", Map.of(
                                "accounts", List.of(
                                        Map.of(
                                                "bban", "15030012345",
                                                "iban", "NO0015030012345",
                                                "currency", "NOK",
                                                "name", "Primary Salary Account",
                                                "ownerName", "Demo Customer",
                                                "status", "enabled",
                                                "usage", "PRIV",
                                                "balances", List.of())))));

        mockMvc.perform(get("/api/v1/gateway/aspsps/dnb/accounts")
                        .header("X-Correlation-Id", "corr-2")
                        .header("Consent-ID", "GW-CONSENT-TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountId").value("15030012345"));

        verify(adapterDnbClientService).fetchAccounts("corr-2", "CONSENT-STATIC-001");
    }
}
