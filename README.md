# psd2-gateway-app

Spring Boot 3 / Java 17 backend behind Kong, with an optional local observability profile that pushes logs into Splunk through Fluent Bit.

## Structure

- `src/` contains the Spring Boot application
- `deploy/` contains Docker Compose, Kong, and observability deployment assets
- `deploy/kong/` contains db-less Kong configuration
- `deploy/observability/fluent-bit/` contains the log forwarder configuration
- `scripts/` contains repeatable local run commands
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
- Kong admin API: `http://localhost:8001/services`
- Kong status: `http://localhost:8100/status`
- Splunk Web: `http://localhost:18000`
- Splunk HEC: `https://localhost:18088`
- Splunk management API: `https://localhost:18089`

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
