package com.example.mockdnbbank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InternalAccessPolicyLoader {

    private final InternalAccessPolicy policyProperties;

    public InternalAccessPolicyLoader(InternalAccessPolicy policyProperties) {
        this.policyProperties = policyProperties;
    }

    public List<InternalAccessPolicy.ClientRule> loadClientRules() throws IOException {
        if (policyProperties.getPath() == null || policyProperties.getPath().isBlank()) {
            return Collections.emptyList();
        }

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        PolicyDocument document = objectMapper.readValue(new File(policyProperties.getPath()), PolicyDocument.class);
        return document.getClients() == null ? Collections.emptyList() : document.getClients();
    }

    public static class PolicyDocument {

        private List<InternalAccessPolicy.ClientRule> clients;

        public List<InternalAccessPolicy.ClientRule> getClients() {
            return clients;
        }

        public void setClients(List<InternalAccessPolicy.ClientRule> clients) {
            this.clients = clients;
        }
    }
}
