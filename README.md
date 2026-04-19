# psd2-gateway-app

Spring Boot 3 / Java 17 backend behind Kong, with an optional local observability profile that pushes logs into Splunk through Fluent Bit.

## Structure

- `src/` contains the Spring Boot application
- `deploy/` contains Docker Compose, Kong, and observability deployment assets
- `deploy/kong/` contains db-less Kong configuration
- `deploy/certs/` contains TLS asset documentation and generated local development certificates
- `deploy/nginx/` contains the mTLS edge configuration
- `deploy/observability/fluent-bit/` contains the log forwarder configuration
- `scripts/` contains repeatable local run commands
- `tpp-client-app/` contains a separate Spring Boot client app acting as a sample TPP
- `adapter-dnb/` contains the first ASPSP-specific internal adapter
- `mock-dnb-bank/` contains a DNB-like mock ASPSP behind mTLS
- `aspsp specs/dnb/` contains the downloaded DNB AIS and PIS specs
- `runtime-logs/` is the local bind-mounted log area for app and Kong logs
- `CONTEXT.md` captures the working context for the repo
- `deploy/observability/CONTEXT.md` captures the observability setup context

## CI/CD Direction

The repo is organized so application code stays at the root while runtime and deployment assets live under `deploy/`. This makes it easier to add CI/CD pipelines that:

- run Maven tests against the app
- build the application image
- validate Compose and gateway config
- promote deployment assets by environment later

## Start App + Kong

```bash
cd /Users/anvarshameemks/psd2-gateway-app
docker compose -f deploy/docker-compose.yml up --build
```

Generate local development TLS certificates first:

```bash
./scripts/generate-dev-certs.sh
```

## Start App + Kong + Splunk

Copy the example environment file once:

```bash
cd /Users/anvarshameemks/psd2-gateway-app
cp .env.example .env
```

Then start the full stack:

```bash
docker compose --env-file .env -f deploy/docker-compose.yml --profile observability up --build
```

On Apple Silicon Macs, Splunk runs under amd64 emulation because the official `splunk/splunk` image is x86-64 only.

## Endpoints

- Kong proxy: `http://localhost:8000/psd2/status`
- NGINX mTLS edge: `https://localhost:8443/psd2/status`
- Kong admin API: `http://localhost:8001/services`
- Kong status: `http://localhost:8100/status`
- Splunk Web: `http://localhost:18000`
- Splunk HEC: `https://localhost:18088`
- Splunk management API: `https://localhost:18089`
- TPP demo app: `http://localhost:8085/api/v1/tpp/gateway-status`

## TPP Demo

Start the sample TPP app with the base stack:

```bash
docker compose -f deploy/docker-compose.yml --profile tpp-demo up --build
```

Then call the TPP app:

```bash
curl -i http://localhost:8085/api/v1/tpp/gateway-status
```

The TPP app calls the NGINX edge over HTTPS and presents its client certificate. NGINX validates the client certificate against the local development CA, then forwards the request to Kong.

## Mutual TLS Note

The industry-standard model is that a TPP presents a client certificate and the API provider allows only approved certificates or approved issuing CAs.

This repo now includes local development certificates for the TPP and the gateway, and the TPP client is wired to present its client certificate. NGINX performs the client-certificate validation at the edge before forwarding to Kong.

So the current repo implements:

- HTTPS termination at the NGINX edge
- client-certificate validation at the NGINX edge
- Kong behind NGINX for routing, rate limiting, and logging
- a sample TPP app calling the edge over HTTPS

This is the OSS-friendly version of the production pattern:

- NGINX handles mTLS transport security
- Kong handles API gateway behavior

If you later move to Kong Enterprise, you can choose to collapse more of the certificate-auth responsibility into Kong.

## Internal Adapter Demo

The first internal zero-trust slice is implemented with `adapter-dnb` and `mock-dnb-bank`.

Current internal flow:

- `psd2-gateway-app` calls `adapter-dnb` over HTTPS with mTLS
- `adapter-dnb` validates the gateway certificate
- `adapter-dnb` checks the caller DN and endpoint against `deploy/security/internal-access-policy.yml`
- `adapter-dnb` calls `mock-dnb-bank` over HTTPS with mTLS
- `mock-dnb-bank` validates the adapter certificate
- `mock-dnb-bank` checks the caller DN and endpoint against `deploy/security/mock-dnb-access-policy.yml`
- the gateway returns a canonical response body to the caller

Gateway canonical endpoints:

```bash
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts/summary
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts -H 'Consent-ID: CONSENT-STATIC-001'
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts/15030012345 -H 'Consent-ID: CONSENT-STATIC-001'
curl -i http://localhost:8000/psd2/aspsps/dnb/accounts/15030012345/balances -H 'Consent-ID: CONSENT-STATIC-001'
curl -i -X POST http://localhost:8000/psd2/aspsps/dnb/consents -H 'Content-Type: application/json' -H 'PSU-ID: demo-psu' -H 'TPP-Redirect-URI: https://tpp.local/callback' -d '{"access":{"accounts":[{"iban":"NO0015030012345"}],"balances":[{"iban":"NO0015030012345"}],"transactions":[{"iban":"NO0015030012345"}]},"recurringIndicator":true,"validUntil":"2026-12-31","frequencyPerDay":4,"combinedServiceIndicator":false}'
```

## Live Logs

Tail the application log:

```bash
tail -f /Users/anvarshameemks/psd2-gateway-app/runtime-logs/app/application.log
```

Tail Kong logs:

```bash
tail -f /Users/anvarshameemks/psd2-gateway-app/runtime-logs/kong/proxy-access.log
tail -f /Users/anvarshameemks/psd2-gateway-app/runtime-logs/kong/proxy-error.log
```

## Helper Scripts

```bash
./scripts/up-local.sh
./scripts/up-observability.sh
./scripts/down.sh
```

## Splunk Login

- username: `admin`
- password: value from `.env`
