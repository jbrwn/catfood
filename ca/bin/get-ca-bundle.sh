#!/bin/bash

set -eu
source "$(dirname "$0")/common.sh"

CA_ROOT_DIR=""
CA_COMMON_NAMES=()
while getopts "d:,n:" flag
do
    case "${flag}" in
        d) CA_ROOT_DIR=${OPTARG};;
        n) CA_COMMON_NAMES+=("${OPTARG}");;
    esac
done

if [ -z "${CA_ROOT_DIR}" ]; then
    echo "Root directory argument -d cannot be empty"
    exit 1
fi

if [ ${#CA_COMMON_NAMES[@]} -eq 0 ]; then
    echo "Common name argument -n cannot be empty"
    exit 1
fi

if [ ! -d "${CA_ROOT_DIR}" ]; then
    echo "Signing CA does not exist!"
    exit 1
fi

for CA_NAME in "${CA_COMMON_NAMES[@]}"; do
  CA_PATH=$(to_dash_case "${CA_NAME}")
  cat ${CA_ROOT_DIR}/${CA_PATH}/certs/${CA_PATH}.cert.pem
done
