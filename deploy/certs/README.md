# TLS Assets

This directory holds local development TLS assets for the Kong HTTPS endpoint and the sample TPP client.

## Generate certificates

Run:

```bash
./scripts/generate-dev-certs.sh
```

Generated files are written to `deploy/certs/generated/` and are ignored by git.

The script creates:

- a local CA certificate
- a Kong server certificate and key
- a sample TPP client certificate
- a PKCS12 key store for the TPP app
- a trust store for the TPP app

## Why this exists

For the local POC:

- Kong terminates HTTPS using the generated server certificate
- the sample TPP app trusts the generated CA
- the sample TPP app is also prepared to present a client certificate

## Industry-standard next step

In production, a TPP typically shares its public certificate or issuing CA chain, and the API provider allows only approved certificates or approved issuing authorities.

With the current OSS Kong image in this repo, HTTPS termination is implemented, but gateway-side certificate allowlisting is not fully enforced by Kong itself. That next step requires Kong Enterprise `mtls-auth` or another mTLS-capable edge.
