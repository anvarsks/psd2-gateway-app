# DNB Adapter Context

## Goal

Provide the first ASPSP-specific adapter for internal zero-trust development.

## Current Scope

- exposes the first DNB AIS internal endpoint slice
- requires mTLS from the gateway
- authorizes requests using the shared YAML internal access policy
- calls `mock-dnb-bank` over outbound mTLS
- returns DNB-shaped payloads that the gateway normalizes

## Current Endpoints

- `GET /internal/dnb/accounts/summary`
- `POST /internal/dnb/consents`
- `GET /internal/dnb/consents/{consentId}`
- `DELETE /internal/dnb/consents/{consentId}`
- `GET /internal/dnb/consents/{consentId}/status`
- `GET /internal/dnb/accounts`
- `GET /internal/dnb/accounts/{accountNumber}`
- `GET /internal/dnb/accounts/{accountNumber}/balances`

## Current Policy

Allowed caller:

- `CN=psd2-gateway-app,O=PSD2 Gateway,C=NO`

Policy source:

- `deploy/security/internal-access-policy.yml`

Outbound bank target:

- `https://mock-dnb-bank:9444/v1`
