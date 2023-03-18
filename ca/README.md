# Catfood Certificate Authority

## Publish
```bash
cd ca
fly auth docker
docker build . -t registry.fly.io/catfood-ca:latest
docker push registry.fly.io/catfood-ca:latest
fly deploy
```
## Spin Up a Container
Create the fly.io machine
```bash
fly machine run registry.fly.io/catfood-ca:latest sleep infinity \
    --volume catfood_ca_data:/ca \
    --region den --app catfood-ca \
    --skip-dns-registration
```
SSH
```bash
fly ssh console -a catfood-ca
```
SFTP
```bash
fly ssh sftp shell -a catfood-ca
```
Clean up
```bash
fly machine destroy <id> --force
```
