# Observability Context

## Goal

Provide a local production-like observability path for `psd2-gateway-app` with:

- continuously tail-able local log files
- Splunk Enterprise for searching logs
- Fluent Bit forwarding logs into Splunk over HEC

## Design

The repo keeps the app, gateway, and observability assets together so the full setup can be cloned and started from a single repository.

The runtime split is:

- Spring Boot writes to `/var/log/psd2-gateway-app/application.log`
- Kong writes access and error logs to `/var/log/kong/*.log`
- bind mounts expose those logs under `runtime-logs/` on the host
- Fluent Bit tails those files and forwards them to Splunk HEC
- Splunk Enterprise provides the searchable UI and HEC endpoint

## Start Commands

Base stack:

```bash
docker compose -f deploy/docker-compose.yml up --build
```

Full stack with observability:

```bash
cp .env.example .env
docker compose --env-file .env -f deploy/docker-compose.yml --profile observability up --build
```

## Splunk Ports

- Web UI: `18000`
- HEC: `18088`
- Management API: `18089`

## Notes

- Splunk is optional through the `observability` profile
- on Apple Silicon Macs, Splunk runs with `platform: linux/amd64`
- runtime logs are local bind mounts and are not committed
- `.env` should stay local and should not be committed
