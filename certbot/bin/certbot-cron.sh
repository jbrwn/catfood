#!/bin/sh

set -eu

echo "Creating cron job for certbot..."
echo "00 00,12 * * * /opt/certbot/certbot-run.sh" >> /etc/crontabs/root

echo "Starting crond"
crond -f -l 8
