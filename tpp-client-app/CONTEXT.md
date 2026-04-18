# TPP Client Context

## Goal

Provide a separate Spring Boot application that acts as a sample TPP consuming the PSD2 gateway over HTTPS.

## Current Flow

- the TPP app calls Kong at `https://kong-psd2-gateway:8443/psd2/status`
- Kong terminates TLS and forwards the request to `psd2-gateway-app`
- the TPP app trusts the local development CA generated under `deploy/certs/generated/`
- the TPP app is also configured to present its own client certificate for future mTLS enforcement

## Important Limitation

The TPP client certificate is prepared and used by the sample TPP app, but the current repo uses OSS Kong. Full gateway-side certificate allowlisting requires Kong Enterprise `mtls-auth` or another mTLS-capable edge.
