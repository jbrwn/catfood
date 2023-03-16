#!/bin/bash
set -eu

DIR=$(dirname "$0")

IN=""
ENV_VAR_NAME=""
while getopts "i:,e:" flag
do
    case "${flag}" in
        i) IN=${OPTARG};;
        e) ENV_VAR_NAME=${OPTARG};;
    esac
done

echo "Creating ${ENV_VAR_NAME}.env..."
echo "${ENV_VAR_NAME}=\"$(cat ${IN})\"" > "${DIR}/${ENV_VAR_NAME}.env"


