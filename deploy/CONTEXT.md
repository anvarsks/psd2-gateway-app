# Deployment Context

## Goal

Keep runtime and deployment assets separate from application source code so the repo is easier to evolve into CI/CD pipelines and multi-app local environments.

## Contents

- `deploy/docker-compose.yml` for local orchestration
- `deploy/kong/` for Kong db-less gateway config
- `deploy/nginx/` for the public inbound mTLS edge
- `deploy/outbound-nginx/` for the outbound DNB API gateway
- `deploy/certs/` for TLS asset documentation and generated development certificates
- `deploy/observability/` for Splunk and Fluent Bit configuration context
- `deploy/security/` for internal service-to-service authorization policies

## Current Profiles

- default: Spring Boot app plus Kong
- `observability`: adds Splunk and Fluent Bit
- `tpp-demo`: adds the sample TPP client app

Current default runtime now includes:

- `psd2-gateway-app`
- `adapter-dnb`
- `outbound-dnb-apigw`
- `mock-dnb-bank`
- `kong`
- `nginx-edge`

## TLS Note

NGINX terminates HTTPS on port `8443` using locally generated development certificates and validates the sample TPP client certificate against the generated CA.

Kong stays behind NGINX and handles gateway concerns such as routing and rate limiting.

## Planned Internal Security Direction

The deployment layer is also planned to grow into a zero-trust internal network model.

Planned additions:

- internal certificates per service
- adapter services for individual ASPSPs
- a central authorization policy file such as `deploy/security/internal-access-policy.yml`

Planned policy model:

- the gateway presents its own client certificate to adapter services
- adapter services validate the gateway certificate
- adapter services allow only explicitly configured client DN and endpoint combinations

## Current Internal Adapter Implementation

The first internal adapter implementation now exists:

- `adapter-dnb`
- `deploy/security/internal-access-policy.yml`

Current enforced rules:

- `psd2-gateway-app` certificate DN is allowed to call the internal DNB adapter endpoints
- `adapter-dnb` certificate DN is allowed to call the outbound DNB API gateway
- `outbound-dnb-apigw` certificate DN is allowed to call the mock DNB AIS endpoints
- unspecified callers or endpoints are rejected

Latest verification:

- local Compose stack started successfully with the outbound gateway in place
- `GET /psd2/aspsps/dnb/accounts` returned `200` through Kong
- outbound gateway access log showed adapter client DN `CN=adapter-dnb,O=Adapter DNB,C=NO`
