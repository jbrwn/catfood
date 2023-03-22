#!/bin/sh

if [ ! -z "$SSL_KEY" ]; then
    echo "SSL_KEY env var found. Creating /etc/nginx/ssl/privkey.pem"
    echo "$SSL_KEY" > /etc/nginx/ssl/privkey.pem
fi

if [ ! -z "$SSL_CERT" ]; then
    echo "SSL_CERT env var found. Creating /etc/nginx/ssl/fullchain.pem"
    echo "$SSL_CERT" > /etc/nginx/ssl/fullchain.pem
fi

if [ ! -z "$SSL_CLIENT_CERT" ]; then
    echo "SSL_CLIENT_CERT env var found. Creating /etc/nginx/ssl/ca.pem"
    echo "$SSL_CLIENT_CERT" > /etc/nginx/ssl/ca.pem
fi
