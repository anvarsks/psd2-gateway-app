# psd2-gateway-app Context

## Current Goal

Run `psd2-gateway-app` behind Kong API Gateway as a production-like local POC with:

- a Spring Boot Java 17 backend service
- a `GET` API exposed through Kong
- service-side logging when the API is hit
- containerized local orchestration for end-to-end testing
- optional Splunk-based observability for continuous log viewing and search

## Planned Target Architecture

The next planned evolution is to turn `psd2-gateway-app` into an aggregator that calls ASPSP-specific adapter services internally and always returns a unified common response format to the TPP.

Planned internal adapter examples:

- `adapter-dnb`
- `adapter-nordea`
- `adapter-tietoevry`

Planned traffic path:

- `TPP -> NGINX mTLS edge -> Kong -> psd2-gateway-app -> ASPSP adapter -> ASPSP`

The gateway will own:

- orchestration across adapters
- canonical request and response mapping
- unified error handling for TPP consumers

The adapter services will own:

- ASPSP-specific protocol and payload handling
- transformation from ASPSP-specific formats into canonical gateway models

## Current Implementation

- Framework: Spring Boot 3.2.5
- Java version: 17
- Build tool: Maven
- App name: `psd2-gateway-app`
- Backend port: `8080`
- Kong proxy port: `8000`
- NGINX mTLS edge port: `8443`
- Kong admin port: `8001`
- Kong status port: `8100`
- Splunk Web port: `18000`
- Splunk HEC port: `18088`
- Splunk management port: `18089`
- Runtime: Docker Compose
- Deployment assets live under: `deploy/`
- Sample TPP client app lives under: `tpp-client-app/`
- First ASPSP adapter lives under: `adapter-dnb/`
- Mock DNB bank lives under: `mock-dnb-bank/`
- Downloaded DNB specs live under: `aspsp specs/dnb/`

## API Details

- Backend endpoint: `GET /api/v1/gateway/status`
- Kong-exposed endpoint: `GET /psd2/status`
- Preferred TPP endpoint: `GET https://localhost:8443/psd2/status`
- Response: `psd2-gateway-app is running`

## Logging Behavior

The service logs the Kong-generated correlation ID when the endpoint is called.

Example log:

`GET API has been hit. X-Correlation-Id=<value>`

The application also writes logs to a bind-mounted host path through:

`runtime-logs/app/application.log`

## TLS and TPP Flow

NGINX now sits in front of Kong and terminates HTTPS for the PSD2 gateway using a locally generated server certificate.

The repo also includes a separate Spring Boot sample TPP application under `tpp-client-app/` that:

- calls the NGINX edge over HTTPS
- trusts the local development CA
- presents its own client certificate

NGINX validates the client certificate against the local CA before forwarding to Kong over the internal Docker network.

For local development, certificate assets are generated under:

`deploy/certs/generated/`

## Planned Internal Zero-Trust Model

The intended next security step is zero-trust service-to-service communication inside the provider network.

Planned principle:

- every internal service call is mutually authenticated with mTLS
- every internal service is denied by default unless explicitly allowed by policy

Planned internal callers and targets include:

- `psd2-gateway-app -> adapter-dnb`
- `psd2-gateway-app -> adapter-nordea`
- `psd2-gateway-app -> adapter-tietoevry`

Planned authorization input:

- verified client certificate identity
- endpoint path
- HTTP method
- centrally defined policy file

Planned policy file:

`deploy/security/internal-access-policy.yml`

Planned policy intent:

- identify the calling service by client DN or equivalent certificate identity
- list the allowed adapter endpoints that caller may invoke
- reject any unspecified service-to-service call

## Implemented Internal DNB Slice

The first internal DNB slice is now implemented with:

- `adapter-dnb`
- `mock-dnb-bank`
- the downloaded DNB AIS spec under `aspsp specs/dnb/ais.yaml`

Current internal flow:

- `psd2-gateway-app -> adapter-dnb` over HTTPS with mTLS
- `adapter-dnb` validates the gateway certificate
- `adapter-dnb` enforces caller authorization using `deploy/security/internal-access-policy.yml`
- `adapter-dnb -> mock-dnb-bank` over HTTPS with mTLS
- `mock-dnb-bank` validates the adapter certificate
- `mock-dnb-bank` enforces caller authorization using `deploy/security/mock-dnb-access-policy.yml`
- `psd2-gateway-app` returns a canonical response object to the caller

Implemented internal policy examples:

- client DN `CN=psd2-gateway-app,O=PSD2 Gateway,C=NO`
- may call the internal DNB consent and account endpoints on `adapter-dnb`
- client DN `CN=adapter-dnb,O=Adapter DNB,C=NO`
- may call the DNB-shaped `/v1` AIS endpoints on `mock-dnb-bank`

Implemented public gateway endpoints:

- `POST /api/v1/gateway/aspsps/dnb/consents`
- `GET /api/v1/gateway/aspsps/dnb/consents/{consentId}`
- `DELETE /api/v1/gateway/aspsps/dnb/consents/{consentId}`
- `GET /api/v1/gateway/aspsps/dnb/consents/{consentId}/status`
- `GET /api/v1/gateway/aspsps/dnb/accounts`
- `GET /api/v1/gateway/aspsps/dnb/accounts/{accountNumber}`
- `GET /api/v1/gateway/aspsps/dnb/accounts/{accountNumber}/balances`
- `GET /api/v1/gateway/aspsps/dnb/accounts/summary`

## Gateway Behavior

Kong is configured in db-less mode and routes traffic to the Spring Boot service over the internal Docker network.

Configured gateway features:

- route path prefix: `/psd2`
- `correlation-id` plugin enabled
- `rate-limiting` plugin enabled with a local policy
- Kong forwards traffic to `http://psd2-gateway-app:8080/api/v1/gateway`
- NGINX terminates TLS on `8443` and forwards to Kong on `8000`

Example:

- incoming request: `GET /psd2/status`
- upstream request: `GET /api/v1/gateway/status`

## Local Run Commands

Build and start the full POC:

```bash
cd /Users/anvarshameemks/psd2-gateway-app && ./scripts/generate-dev-certs.sh && docker compose -f deploy/docker-compose.yml up --build
```

Build and start the full POC with Splunk observability:

```bash
cd /Users/anvarshameemks/psd2-gateway-app && cp .env.example .env && docker compose --env-file .env -f deploy/docker-compose.yml --profile observability up --build
```

Stop the stack:

```bash
cd /Users/anvarshameemks/psd2-gateway-app && docker compose --env-file .env -f deploy/docker-compose.yml down
```

## Health Endpoints

- backend health: `http://localhost:8080/actuator/health` inside the app container
- Kong status health: `http://localhost:8100/status`
- Splunk Web UI: `http://localhost:18000`

## Test Commands

Call the app through Kong:

```bash
curl -i http://localhost:8000/psd2/status
```

Optional gateway status check:

```bash
curl -i http://localhost:8100/status
```

Optional Kong admin API check:

```bash
curl -i http://localhost:8001/services
```

Tail application logs continuously from the host:

```bash
tail -f /Users/anvarshameemks/psd2-gateway-app/runtime-logs/app/application.log
```

Call the gateway over HTTPS:

```bash
curl -k -i https://localhost:8443/psd2/status
```

Call the sample TPP app:

```bash
curl -i http://localhost:8085/api/v1/tpp/gateway-status
```

Call the first aggregator adapter endpoint:

```bash
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts/summary
```

Create a DNB consent through the gateway:

```bash
curl -i -X POST http://localhost:8000/psd2/aspsps/dnb/consents \
  -H 'Content-Type: application/json' \
  -H 'PSU-ID: demo-psu' \
  -H 'TPP-Redirect-URI: https://tpp.local/callback' \
  -d '{"access":{"accounts":[{"iban":"NO0015030012345"}],"balances":[{"iban":"NO0015030012345"}],"transactions":[{"iban":"NO0015030012345"}]},"recurringIndicator":true,"validUntil":"2026-12-31","frequencyPerDay":4,"combinedServiceIndicator":false}'
```

Read DNB accounts through the gateway:

```bash
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts \
  -H 'Consent-ID: CONSENT-STATIC-001'
```

## Expected Result

- HTTP status: `200 OK`
- Response body: `psd2-gateway-app is running`
- application log includes the propagated `X-Correlation-Id`
- response includes `X-Correlation-Id`

## History Retention

With the current observability setup, history is preserved across normal restarts if these are kept intact:

- host log files under `runtime-logs/`
- Docker volume `splunk-data`
- Docker volume `fluent-bit-state`

History can be lost if any of the following are deleted or reset:

- `runtime-logs/app/`
- `runtime-logs/kong/`
- `splunk-data` volume
- `fluent-bit-state` volume

Operational rule:

- use `docker compose ... down` for normal stops
- avoid `docker compose ... down -v` unless you intentionally want to wipe retained observability state

## Current User Activity

As of April 17, 2026, the work has moved from a direct Spring Boot run to a Kong-fronted POC so the app can be exercised through the API gateway instead of calling the backend directly.

The latest change propagates Kong's `X-Correlation-Id` into the Spring Boot controller and logs that value in the service layer for request tracing.

The repo now also includes an observability profile that adds Splunk Enterprise and Fluent Bit without splitting the work into a second repository.

On Apple Silicon Macs, the Splunk container is forced to `linux/amd64` because the official image is x86-64 only.

The deployment layout has been reorganized for CI/CD readiness so Compose, Kong, and observability assets live under `deploy/` instead of the repo root.

The repo now also includes an OSS-friendly mTLS edge:

- NGINX validates the TPP client certificate
- Kong remains responsible for API gateway behavior
- the sample `tpp-client-app` exercises the HTTPS client flow

The next planned stage is internal zero-trust communication between `psd2-gateway-app` and future ASPSP adapter services, controlled by a YAML policy that maps caller certificate identity to allowed adapter endpoints.

The first stage of that plan is now implemented with `adapter-dnb` and a YAML policy allowing only the gateway service DN to call the DNB summary endpoint.
