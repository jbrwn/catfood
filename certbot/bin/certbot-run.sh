#!/bin/sh

set -eu

echo "Executing certbot..."
certbot certonly \
        --noninteractive --agree-tos --email ${LETSENCRYPT_ACCOUNT_EMAIL} \
        --authenticator 'dns-google-domains' \
        --dns-google-domains-credentials '/var/lib/letsencrypt/dns_google_domains_credentials.ini' \
        --dns-google-domains-zone 'catfood.dog' \
        --deploy-hook /opt/certbot/deploy-hook.sh \
        --cert-name catfood.dog \
        -d catfood.dog -d www.catfood.dog -d device.catfood.dog -d app.catfood.dog -d api.catfood.dog
