# Catfood Proxy

## Publish
```bash
cd proxy
fly auth docker
docker build . -t registry.fly.io/catfood-proxy:latest
docker push registry.fly.io/catfood-proxy:latest
fly deploy
```
