#!/bin/bash

set -eu
source "$(dirname "$0")/common.sh"

CA_ROOT_DIR=""
CA_COMMON_NAME=""
CA_CERT_TYPE=""
while getopts "d:,n:,t:" flag
do
    case "${flag}" in
        d) CA_ROOT_DIR=${OPTARG};;
        n) CA_COMMON_NAME=${OPTARG};;
        t) CA_CERT_TYPE=${OPTARG};;
    esac
done

if [ -z "${CA_ROOT_DIR}" ]; then
    echo "Root directory argument -d cannot be empty"
    exit 1
fi

if [ -z "${CA_COMMON_NAME}" ]; then
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

CA_SIGNER_COMMON_NAME=$(cat ${CA_ROOT_DIR}/_name)
CA_SIGNER_FILE_PREFIX=$(to_dash_case "${CA_SIGNER_COMMON_NAME}")
CA_SIGNER_KEY_PASSWORD_VAR_NAME=CA_$(to_underscore_case "${CA_SIGNER_COMMON_NAME}")_KEY_PASSWORD
CA_SIGNER_KEY_PASSWORD=${!CA_SIGNER_KEY_PASSWORD_VAR_NAME}

if [ -z "${CA_SIGNER_KEY_PASSWORD}" ]; then
    echo "${CA_SIGNER_KEY_PASSWORD_VAR_NAME} env var not found."
    exit 1
fi

CA_CERT_FILE_PREFIX=$(to_dash_case "${CA_COMMON_NAME}")

if [ ! -f "${CA_ROOT_DIR}/certs/${CA_CERT_FILE_PREFIX}.cert.pem" ]; then
    echo "${CA_CERT_FILE_PREFIX}.cert.pem does not exist!"
    exit 1
fi

cd "${CA_ROOT_DIR}"
echo "Setting permissions..."
chmod 644 "certs/${CA_CERT_FILE_PREFIX}.cert.pem"
echo "Singing CSR..."
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
echo "Setting permissions..."
chmod 644 "certs/${CA_CERT_FILE_PREFIX}-chain.cert.pem"
echo "Creating CA chain..."
cat  "certs/${CA_CERT_FILE_PREFIX}.cert.pem" \
      "certs/${CA_SIGNER_FILE_PREFIX}.cert.pem" > "certs/${CA_CERT_FILE_PREFIX}-chain.cert.pem"
echo "Setting permissions..."
chmod 444 "certs/${CA_CERT_FILE_PREFIX}-chain.cert.pem"
