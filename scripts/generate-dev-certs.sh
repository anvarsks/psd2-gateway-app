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

cat > "$TMP_DIR/ca-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
x509_extensions = v3_ca

[ req_distinguished_name ]
C = NO
O = PSD2 Gateway
CN = psd2-dev-ca

[ v3_ca ]
basicConstraints = critical, CA:true
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
EOF

openssl genrsa -out "$CERT_DIR/ca.key" 2048
openssl req -x509 -new -nodes -key "$CERT_DIR/ca.key" -sha256 -days 3650 \
  -out "$CERT_DIR/ca.crt" \
  -config "$TMP_DIR/ca-openssl.cnf" \
  -extensions v3_ca

generate_client_certificate() {
  local file_prefix="$1"
  local subject_cn="$2"
  local org="$3"
  local dns_name="$4"

  cat > "$TMP_DIR/$file_prefix-openssl.cnf" <<EOF
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = $org
CN = $subject_cn

[ req_ext ]
subjectAltName = @alt_names
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = $dns_name
EOF

  openssl genrsa -out "$CERT_DIR/$file_prefix.key" 2048
  openssl req -new -key "$CERT_DIR/$file_prefix.key" \
    -out "$TMP_DIR/$file_prefix.csr" \
    -config "$TMP_DIR/$file_prefix-openssl.cnf"
  openssl x509 -req -in "$TMP_DIR/$file_prefix.csr" \
    -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
    -out "$CERT_DIR/$file_prefix.crt" -days 825 -sha256 \
    -extfile "$TMP_DIR/$file_prefix-openssl.cnf" -extensions req_ext

  openssl pkcs12 -export \
    -inkey "$CERT_DIR/$file_prefix.key" \
    -in "$CERT_DIR/$file_prefix.crt" \
    -certfile "$CERT_DIR/ca.crt" \
    -out "$CERT_DIR/$file_prefix.p12" \
    -passout "pass:$PASSWORD" \
    -name "$file_prefix"
}

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
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = localhost
DNS.2 = nginx-psd2-edge
DNS.3 = kong-psd2-gateway
DNS.4 = host.docker.internal
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
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = tpp-client-app
DNS.2 = sample-tpp
EOF

generate_client_certificate "tpp-client" "tpp-client-app" "Sample TPP" "tpp-client-app"
generate_client_certificate "gateway-client" "psd2-gateway-app" "PSD2 Gateway" "psd2-gateway-app"

cat > "$TMP_DIR/adapter-dnb-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = Adapter DNB
CN = adapter-dnb

[ req_ext ]
subjectAltName = @alt_names
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = adapter-dnb
EOF

openssl genrsa -out "$CERT_DIR/adapter-dnb.key" 2048
openssl req -new -key "$CERT_DIR/adapter-dnb.key" \
  -out "$TMP_DIR/adapter-dnb.csr" \
  -config "$TMP_DIR/adapter-dnb-openssl.cnf"
openssl x509 -req -in "$TMP_DIR/adapter-dnb.csr" \
  -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
  -out "$CERT_DIR/adapter-dnb.crt" -days 825 -sha256 \
  -extfile "$TMP_DIR/adapter-dnb-openssl.cnf" -extensions req_ext

openssl pkcs12 -export \
  -inkey "$CERT_DIR/adapter-dnb.key" \
  -in "$CERT_DIR/adapter-dnb.crt" \
  -certfile "$CERT_DIR/ca.crt" \
  -out "$CERT_DIR/adapter-dnb.p12" \
  -passout "pass:$PASSWORD" \
  -name "adapter-dnb"

cat > "$TMP_DIR/mock-dnb-bank-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = Mock DNB
CN = mock-dnb-bank

[ req_ext ]
subjectAltName = @alt_names
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = mock-dnb-bank
EOF

openssl genrsa -out "$CERT_DIR/mock-dnb-bank.key" 2048
openssl req -new -key "$CERT_DIR/mock-dnb-bank.key" \
  -out "$TMP_DIR/mock-dnb-bank.csr" \
  -config "$TMP_DIR/mock-dnb-bank-openssl.cnf"
openssl x509 -req -in "$TMP_DIR/mock-dnb-bank.csr" \
  -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
  -out "$CERT_DIR/mock-dnb-bank.crt" -days 825 -sha256 \
  -extfile "$TMP_DIR/mock-dnb-bank-openssl.cnf" -extensions req_ext

openssl pkcs12 -export \
  -inkey "$CERT_DIR/mock-dnb-bank.key" \
  -in "$CERT_DIR/mock-dnb-bank.crt" \
  -certfile "$CERT_DIR/ca.crt" \
  -out "$CERT_DIR/mock-dnb-bank.p12" \
  -passout "pass:$PASSWORD" \
  -name "mock-dnb-bank"

cat > "$TMP_DIR/outbound-dnb-apigw-openssl.cnf" <<'EOF'
[ req ]
distinguished_name = req_distinguished_name
prompt = no
req_extensions = req_ext

[ req_distinguished_name ]
C = NO
O = Outbound API Gateway
CN = outbound-dnb-apigw

[ req_ext ]
subjectAltName = @alt_names
basicConstraints = critical, CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectKeyIdentifier = hash

[ alt_names ]
DNS.1 = outbound-dnb-apigw
EOF

openssl genrsa -out "$CERT_DIR/outbound-dnb-apigw.key" 2048
openssl req -new -key "$CERT_DIR/outbound-dnb-apigw.key" \
  -out "$TMP_DIR/outbound-dnb-apigw.csr" \
  -config "$TMP_DIR/outbound-dnb-apigw-openssl.cnf"
openssl x509 -req -in "$TMP_DIR/outbound-dnb-apigw.csr" \
  -CA "$CERT_DIR/ca.crt" -CAkey "$CERT_DIR/ca.key" -CAcreateserial \
  -out "$CERT_DIR/outbound-dnb-apigw.crt" -days 825 -sha256 \
  -extfile "$TMP_DIR/outbound-dnb-apigw-openssl.cnf" -extensions req_ext

rm -f "$CERT_DIR/tpp-truststore.p12"
keytool -importcert -noprompt \
  -alias psd2-dev-ca \
  -file "$CERT_DIR/ca.crt" \
  -keystore "$CERT_DIR/tpp-truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD"

rm -f "$CERT_DIR/gateway-truststore.p12"
keytool -importcert -noprompt \
  -alias psd2-dev-ca \
  -file "$CERT_DIR/ca.crt" \
  -keystore "$CERT_DIR/gateway-truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD"

rm -f "$CERT_DIR/adapter-dnb-truststore.p12"
keytool -importcert -noprompt \
  -alias psd2-dev-ca \
  -file "$CERT_DIR/ca.crt" \
  -keystore "$CERT_DIR/adapter-dnb-truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD"

rm -f "$CERT_DIR/mock-dnb-bank-truststore.p12"
keytool -importcert -noprompt \
  -alias psd2-dev-ca \
  -file "$CERT_DIR/ca.crt" \
  -keystore "$CERT_DIR/mock-dnb-bank-truststore.p12" \
  -storetype PKCS12 \
  -storepass "$PASSWORD"

echo "Generated local TLS assets under $CERT_DIR"
