#!/bin/sh

set -eu

# Unset doppler secrets integration variables
# https://community.doppler.com/t/environmental-variable-help/1052
unset DOPPLER_CONFIG DOPPLER_ENVIRONMENT DOPPLER_PROJECT

echo "Pushing ${RENEWED_LINEAGE} certs to doppler..."
cat ${RENEWED_LINEAGE}/privkey.pem | doppler secrets set SSL_KEY --silent
cat ${RENEWED_LINEAGE}/fullchain.pem | doppler secrets set SSL_CERT --silent
