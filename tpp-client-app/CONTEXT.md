# TPP Client Context

## Goal

Provide a separate Spring Boot application that acts as a sample TPP consuming the PSD2 gateway over HTTPS.

## Current Flow

- the TPP app calls NGINX at `https://nginx-psd2-edge:8443/psd2/status`
- NGINX terminates TLS, validates the client certificate, and forwards the request to Kong
- Kong forwards the request to `psd2-gateway-app`
- the TPP app trusts the local development CA generated under `deploy/certs/generated/`
- the TPP app is configured to present its own client certificate during the HTTPS handshake
