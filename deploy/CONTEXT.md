# Deployment Context

## Goal

Keep runtime and deployment assets separate from application source code so the repo is easier to evolve into CI/CD pipelines and multi-app local environments.

## Contents

- `deploy/docker-compose.yml` for local orchestration
- `deploy/kong/` for Kong db-less gateway config
- `deploy/certs/` for TLS asset documentation and generated development certificates
- `deploy/observability/` for Splunk and Fluent Bit configuration context

## Current Profiles

- default: Spring Boot app plus Kong
- `observability`: adds Splunk and Fluent Bit
- `tpp-demo`: adds the sample TPP client app

## TLS Note

NGINX terminates HTTPS on port `8443` using locally generated development certificates and validates the sample TPP client certificate against the generated CA.

Kong stays behind NGINX and handles gateway concerns such as routing and rate limiting.
