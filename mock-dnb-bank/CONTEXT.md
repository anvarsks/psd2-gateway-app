# Mock DNB Bank

- Purpose: local mock ASPSP that exposes a DNB-like AIS surface from the downloaded spec.
- Security: mTLS required on all `/v1/**` endpoints.
- Client authorization: policy-driven allowlist based on client DN and endpoint path.
- Current slice:
  - `POST /v1/consents`
  - `GET /v1/consents/{consentId}`
  - `DELETE /v1/consents/{consentId}`
  - `GET /v1/consents/{consentId}/status`
  - `GET /v1/accounts`
  - `GET /v1/accounts/{accountNumber}`
  - `GET /v1/accounts/{accountNumber}/balances`
