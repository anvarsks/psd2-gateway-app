# psd2-gateway-app Context

## Current Goal

Run `psd2-gateway-app` behind Kong API Gateway as a production-like local POC with:

- a Spring Boot Java 17 backend service
- a `GET` API exposed through Kong
- service-side logging when the API is hit
- containerized local orchestration for end-to-end testing
- optional Splunk-based observability for continuous log viewing and search

## Current Implementation

- Framework: Spring Boot 3.2.5
- Java version: 17
- Build tool: Maven
- App name: `psd2-gateway-app`
- Backend port: `8080`
- Kong proxy port: `8000`
- Kong admin port: `8001`
- Kong status port: `8100`
- Splunk Web port: `18000`
- Splunk HEC port: `18088`
- Splunk management port: `18089`
- Runtime: Docker Compose
- Deployment assets live under: `deploy/`

## API Details

- Backend endpoint: `GET /api/v1/gateway/status`
- Kong-exposed endpoint: `GET /psd2/status`
- Response: `psd2-gateway-app is running`

## Logging Behavior

The service logs the Kong-generated correlation ID when the endpoint is called.

Example log:

`GET API has been hit. X-Correlation-Id=<value>`

The application also writes logs to a bind-mounted host path through:

`runtime-logs/app/application.log`

## Gateway Behavior

Kong is configured in db-less mode and routes traffic to the Spring Boot service over the internal Docker network.

Configured gateway features:

- route path prefix: `/psd2`
- `correlation-id` plugin enabled
- `rate-limiting` plugin enabled with a local policy
- Kong forwards traffic to `http://psd2-gateway-app:8080/api/v1/gateway`

Example:

- incoming request: `GET /psd2/status`
- upstream request: `GET /api/v1/gateway/status`

## Local Run Commands

Build and start the full POC:

```bash
cd /Users/anvarshameemks/psd2-gateway-app && docker compose -f deploy/docker-compose.yml up --build
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

## Expected Result

- HTTP status: `200 OK`
- Response body: `psd2-gateway-app is running`
- application log includes the propagated `X-Correlation-Id`
- response includes `X-Correlation-Id`

## Current User Activity

As of April 17, 2026, the work has moved from a direct Spring Boot run to a Kong-fronted POC so the app can be exercised through the API gateway instead of calling the backend directly.

The latest change propagates Kong's `X-Correlation-Id` into the Spring Boot controller and logs that value in the service layer for request tracing.

The repo now also includes an observability profile that adds Splunk Enterprise and Fluent Bit without splitting the work into a second repository.

On Apple Silicon Macs, the Splunk container is forced to `linux/amd64` because the official image is x86-64 only.

The deployment layout has been reorganized for CI/CD readiness so Compose, Kong, and observability assets live under `deploy/` instead of the repo root.
