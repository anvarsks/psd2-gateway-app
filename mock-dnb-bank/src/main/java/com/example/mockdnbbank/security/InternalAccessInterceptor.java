package com.example.mockdnbbank.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InternalAccessInterceptor implements HandlerInterceptor {

    private final InternalAccessPolicyLoader policyLoader;

    public InternalAccessInterceptor(InternalAccessPolicyLoader policyLoader) {
        this.policyLoader = policyLoader;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        X509Certificate[] certificates =
                (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certificates == null || certificates.length == 0) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Client certificate is required");
            return false;
        }

        String clientDn = certificates[0].getSubjectX500Principal().getName();
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        List<InternalAccessPolicy.ClientRule> clientRules = policyLoader.loadClientRules();
        boolean allowed = clientRules.stream()
                .filter(rule -> clientDn.equals(rule.getClientDn()))
                .flatMap(rule -> rule.getAllowedEndpoints().stream())
                .anyMatch(endpointRule -> method.equalsIgnoreCase(endpointRule.getMethod())
                        && matchesPath(endpointRule.getPath(), requestPath));

        if (!allowed) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Client DN is not allowed to call this endpoint");
            return false;
        }

        return true;
    }

    private boolean matchesPath(String configuredPath, String requestPath) {
        String regex = configuredPath.replaceAll("\\{[^/]+}", "[^/]+");
        return Pattern.matches(regex, requestPath);
    }
}
