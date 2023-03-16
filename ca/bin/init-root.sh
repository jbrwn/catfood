#!/bin/bash

set -eu
source "$(dirname "$0")/common.sh"

CA_ROOT_DIR=""
CA_COUNTRY=""
CA_ORGANIZATION=""
CA_COMMON_NAME=""
while getopts "d:,c:,o:,n:" flag
do
    case "${flag}" in
        d) CA_ROOT_DIR=${OPTARG};;
        c) CA_COUNTRY=${OPTARG};;
        o) CA_ORGANIZATION=${OPTARG};;
        n) CA_COMMON_NAME=${OPTARG};;
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

if [ -z "${CA_COMMON_NAME}" ]; then
    echo "Common name argument -n cannot be empty"
    exit 1
fi

CA_ROOT_KEY_PASSWORD_VAR_NAME=CA_$(to_underscore_case "${CA_COMMON_NAME}")_KEY_PASSWORD
CA_ROOT_KEY_PASSWORD=${!CA_ROOT_KEY_PASSWORD_VAR_NAME}

if [ -z "${CA_ROOT_KEY_PASSWORD}" ]; then
    echo "${CA_ROOT_KEY_PASSWORD_VAR_NAME} env var not found."
    exit 1
fi

export CA_ROOT_DIR=${CA_ROOT_DIR}/$(to_dash_case "${CA_COMMON_NAME}")
export CA_ROOT_FILE_PREFIX=$(to_dash_case "${CA_COMMON_NAME}")

if [ -d "${CA_ROOT_DIR}" ]; then
    echo "Root CA already exists!"
    exit 1
fi

echo "Creating directories..."
mkdir -p "${CA_ROOT_DIR}"
cd "${CA_ROOT_DIR}"
mkdir certs crl newcerts private
echo "Setting permissions..."
chmod 700 private
echo "Creating index file..."
touch index.txt
echo "Creating serial file..."
echo 1000 > serial
echo "Creating _name file..."
echo "${CA_COMMON_NAME}" > _name
echo "Creating root ca openssl config..."
envsubst "$(get_ca_env_var_list)" < /configs/openssl-root.cnf > openssl.cnf
echo "Creating root CA private key..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4046 \
      -des3 -pass pass:${CA_ROOT_KEY_PASSWORD} \
      -out "private/${CA_ROOT_FILE_PREFIX}.key.pem"
echo "Setting permissions..."
chmod 400 "private/${CA_ROOT_FILE_PREFIX}.key.pem"
echo "Creating root CA certificate..."
openssl req -config openssl.cnf \
      -key "private/${CA_ROOT_FILE_PREFIX}.key.pem" \
      -passin pass:${CA_ROOT_KEY_PASSWORD} \
      -subj "/C=${CA_COUNTRY}/O=${CA_ORGANIZATION}/CN=${CA_COMMON_NAME}" \
      -new -x509 -sha256 -days 3650 -extensions v3_ca \
      -out "certs/${CA_ROOT_FILE_PREFIX}.cert.pem"
echo "Setting permissions..."
chmod 444 "certs/${CA_ROOT_FILE_PREFIX}.cert.pem"
echo "Verifying certificate..."
openssl x509 -noout -text -in "certs/${CA_ROOT_FILE_PREFIX}.cert.pem"
