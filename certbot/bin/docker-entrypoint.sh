#!/bin/sh

set -eu

echo "Creating /var/lib/letsencrypt/dns_google_domains_credentials.ini..."
echo "dns_google_domains_access_token = ${GOOGLE_DOMAINS_ACCESS_TOKEN}" > /var/lib/letsencrypt/dns_google_domains_credentials.ini
chmod go-rwx /var/lib/letsencrypt/dns_google_domains_credentials.ini

exec "$@"
