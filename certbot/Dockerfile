FROM ghcr.io/aaomidi/certbot-dns-google-domains:latest

# Add Doppler's RSA key
RUN wget -q -t3 'https://packages.doppler.com/public/cli/rsa.8004D9FF50437357.key' -O /etc/apk/keys/cli@doppler-8004D9FF50437357.rsa.pub

# Add Doppler's apk repo
RUN echo 'https://packages.doppler.com/public/cli/alpine/any-version/main' | tee -a /etc/apk/repositories

# Install latest Doppler CLI
RUN apk add doppler

WORKDIR /opt/certbot
COPY bin/ .

ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["./certbot-cron.sh"]
