#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CERT_DIR="$ROOT_DIR/deploy/certs/generated"
TMP_DIR="$(mktemp -d)"
PASSWORD="${DEV_CERT_PASSWORD:-changeit}"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mkdir -p "$CERT_DIR"

openssl genrsa -out "$CERT_DIR/ca.key" 2048
openssl req -x509 -new -nodes -key "$CERT_DIR/ca.key" -sha256 -days 3650 \
  -out "$CERT_DIR/ca.crt" \
  -subj "/C=NO/O=PSD2 Gateway/CN=psd2-dev-ca"

cat > "$TMP_DIR/kong-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = PSD2 Gateway
CN = localhost

[ req_ext ]
subjectAltName = @alt_names
extendedKeyUsage = serverAuth

[ alt_names ]
DNS.1 = localhost
DNS.2 = kong-psd2-gateway
DNS.3 = host.docker.internal
IP.1 = 127.0.0.1
EOF

openssl genrsa -out "$CERT_DIR/kong.key" 2048
openssl req -new -key "$CERT_DIR/kong.key" \
  -out "$TMP_DIR/kong.csr" \
  -config "$TMP_DIR/kong-openssl.cnf"
openssl x509 -req -in "$TMP_DIR/kong.csr" \
  -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
  -out "$CERT_DIR/kong.crt" -days 825 -sha256 \
  -extfile "$TMP_DIR/kong-openssl.cnf" -extensions req_ext

cat > "$TMP_DIR/tpp-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = Sample TPP
CN = tpp-client-app

[ req_ext ]
subjectAltName = @alt_names
extendedKeyUsage = clientAuth

[ alt_names ]
DNS.1 = tpp-client-app
DNS.2 = sample-tpp
EOF

openssl genrsa -out "$CERT_DIR/tpp-client.key" 2048
openssl req -new -key "$CERT_DIR/tpp-client.key" \
  -out "$TMP_DIR/tpp-client.csr" \
  -config "$TMP_DIR/tpp-openssl.cnf"
openssl x509 -req -in "$TMP_DIR/tpp-client.csr" \
  -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
  -out "$CERT_DIR/tpp-client.crt" -days 825 -sha256 \
  -extfile "$TMP_DIR/tpp-openssl.cnf" -extensions req_ext

openssl pkcs12 -export \
  -inkey "$CERT_DIR/tpp-client.key" \
  -in "$CERT_DIR/tpp-client.crt" \
  -certfile "$CERT_DIR/ca.crt" \
  -out "$CERT_DIR/tpp-client.p12" \
  -passout "pass:$PASSWORD" \
  -name "tpp-client"

rm -f "$CERT_DIR/tpp-truststore.p12"
keytool -importcert -noprompt \
  -alias psd2-dev-ca \
  -file "$CERT_DIR/ca.crt" \
  -keystore "$CERT_DIR/tpp-truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD"

echo "Generated local TLS assets under $CERT_DIR"
