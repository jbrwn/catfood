#!/bin/bash

set -eu
source "$(dirname "$0")/common.sh"

CA_ROOT_DIR=""
CA_CERT_TYPE=""
CA_COUNTRY=""
CA_ORGANIZATION=""
CA_COMMON_NAMES=()
while getopts "d:,c:,o:,n:,t:" flag
do
    case "${flag}" in
        d) CA_ROOT_DIR=${OPTARG};;
        c) CA_COUNTRY=${OPTARG};;
        o) CA_ORGANIZATION=${OPTARG};;
        n) CA_COMMON_NAMES+=("${OPTARG}");;
        t) CA_CERT_TYPE=${OPTARG};;
    esac
done

if [ -z "${CA_ROOT_DIR}" ]; then
    echo "Root directory argument -d cannot be empty"
    exit 1
fi

if [ -z "${CA_COUNTRY}" ]; then
    echo "Country argument -c cannot be empty"
    exit 1
fi

if [ -z "${CA_ORGANIZATION}" ]; then
    echo "Organization argument -o cannot be empty"
    exit 1
fi

if [ ${#CA_COMMON_NAMES[@]} -eq 0 ]; then
    echo "Common name argument -n cannot be empty"
    exit 1
fi

if [ -z "${CA_CERT_TYPE}" ]; then
    echo "Certificate type argument -t cannot be empty"
    exit 1
fi

if [ ! -d "${CA_ROOT_DIR}" ]; then
    echo "Signing CA does not exist!"
    exit 1
fi

CA_COMMON_NAME="${CA_COMMON_NAMES[0]}"
CA_SUBJECT_ALTERNATIVE_NAMES=""
delim=""
for item in "${CA_COMMON_NAMES[@]}"; do
  CA_SUBJECT_ALTERNATIVE_NAMES="${CA_SUBJECT_ALTERNATIVE_NAMES}${delim}DNS:${item}"
  delim=","
done

CA_SIGNER_COMMON_NAME=$(cat ${CA_ROOT_DIR}/_name)
CA_SIGNER_FILE_PREFIX=$(to_dash_case "${CA_SIGNER_COMMON_NAME}")
CA_SIGNER_KEY_PASSWORD_VAR_NAME=CA_$(to_underscore_case "${CA_SIGNER_COMMON_NAME}")_KEY_PASSWORD
CA_SIGNER_KEY_PASSWORD=${!CA_SIGNER_KEY_PASSWORD_VAR_NAME}

if [ -z "${CA_SIGNER_KEY_PASSWORD}" ]; then
    echo "${CA_SIGNER_KEY_PASSWORD_VAR_NAME} env var not found."
    exit 1
fi

CA_CERT_FILE_PREFIX=$(to_dash_case "${CA_COMMON_NAME}")

if [ -f "${CA_ROOT_DIR}/certs/${CA_CERT_FILE_PREFIX}.cert.pem" ]; then
    echo "${CA_CERT_FILE_PREFIX}.cert.pem already exist!"
    exit 1
fi

cd "${CA_ROOT_DIR}"
echo "Creating ${CA_COMMON_NAME} private key..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
      -out "private/${CA_CERT_FILE_PREFIX}.key.pem"
echo "Setting permissions..."
chmod 400 "private/${CA_CERT_FILE_PREFIX}.key.pem"
echo "Generating certificate signing request..."
openssl req -config "openssl.cnf" \
      -subj "/C=${CA_COUNTRY}/O=${CA_ORGANIZATION}/CN=${CA_COMMON_NAME}" \
      -reqexts san -config <(cat /etc/ssl/openssl.cnf <(printf "\n[san]\nsubjectAltName=${CA_SUBJECT_ALTERNATIVE_NAMES}")) \
      -key "private/${CA_CERT_FILE_PREFIX}.key.pem" \
      -new -sha256 -out "csr/${CA_CERT_FILE_PREFIX}.csr.pem"
echo "Signing CSR..."
openssl ca -config "openssl.cnf" \
      -extensions ${CA_CERT_TYPE} -days 365 -notext -md sha256 \
      -passin pass:${CA_SIGNER_KEY_PASSWORD} -batch \
      -in "csr/${CA_CERT_FILE_PREFIX}.csr.pem" \
      -out "certs/${CA_CERT_FILE_PREFIX}.cert.pem"
echo "Setting permissions..."
chmod 444 "certs/${CA_CERT_FILE_PREFIX}.cert.pem"
echo "Verifying certificate..."
openssl x509 -noout -text \
      -in "certs/${CA_CERT_FILE_PREFIX}.cert.pem"
openssl verify -CAfile "certs/${CA_SIGNER_FILE_PREFIX}-chain.cert.pem" \
      "certs/${CA_CERT_FILE_PREFIX}.cert.pem"
echo "Creating CA chain..."
cat  "certs/${CA_CERT_FILE_PREFIX}.cert.pem" \
      "certs/${CA_SIGNER_FILE_PREFIX}.cert.pem" > "certs/${CA_CERT_FILE_PREFIX}-chain.cert.pem"
echo "Setting permissions..."
chmod 444 "certs/${CA_CERT_FILE_PREFIX}-chain.cert.pem"
