package com.example.mockdnbbank.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.policy")
public class InternalAccessPolicy {

    private String path;
    private List<ClientRule> clients = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ClientRule> getClients() {
        return clients;
    }

    public void setClients(List<ClientRule> clients) {
        this.clients = clients;
    }

    public static class ClientRule {

        private String clientDn;
        private List<EndpointRule> allowedEndpoints = new ArrayList<>();

        public String getClientDn() {
            return clientDn;
        }

        public void setClientDn(String clientDn) {
            this.clientDn = clientDn;
        }

        public List<EndpointRule> getAllowedEndpoints() {
            return allowedEndpoints;
        }

        public void setAllowedEndpoints(List<EndpointRule> allowedEndpoints) {
            this.allowedEndpoints = allowedEndpoints;
        }
    }

    public static class EndpointRule {

        private String method;
        private String path;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
