# Catfood Certbot

## Publish
```bash
cd certbot
fly auth docker
docker build . -t registry.fly.io/catfood-certbot:latest
docker push registry.fly.io/catfood-certbot:latest
fly deploy
```