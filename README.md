# Catfood App

## Build
```bash
./gradlew clean assemble
```

## Test

## Publish
```bash
fly auth docker
docker build . -t registry.fly.io/catfood-app:latest
docker push registry.fly.io/catfood-app:latest
fly deploy
```


## Run Locally
Build local certificate authority and create env files.
```bash
docker-compose -f ca/docker-compose.yaml run ca
```
Bring up dependencies in containers.
```bash
docker-compose up -d db redis
```

Run the main app locally with intellij or gradle `./gradlew run`.
The following env vars must be set.  Update accordingly if you modify the defaults in docker-compose.yaml.
```
CA_KEY=ca/_ca/catfood-root-ca/catfood-intermediate-ca-1/private/catfood-intermediate-ca-1.key.pem
CA_CERTIFICATE=ca/_ca/catfood-root-ca/catfood-intermediate-ca-1/certs/catfood-intermediate-ca-1.cert.pem
CA_PASSWORD=catfood
DB_APP_PASSWORD=catfood
DB_APP_URL=r2dbc:pool:postgresql://localhost:6000/catfood?maxIdleTime=PT60S
DB_APP_USER=catfood
DB_MIGRATION_PASSWORD=catfood
DB_MIGRATION_URL=jdbc:postgresql://localhost:6000/catfood?sslmode=disable
DB_MIGRATION_USER=catfood
DEVELOPMENT=true
REDIS_PASSWORD=
REDIS_URL=redis://localhost:6001
```

## Run Locally With Docker
Run the app locally with all associated infrastructure.
This can be helpful for end e2e testing or debugging issues related to certificate authentication.

Build local certificate authority and create env files.
```bash
docker-compose -f ca/docker-compose.yaml run ca
```
Build the app
```bash
./gradlew clean assemble
```
Bring up the app and all dependencies in containers
```bash
docker-compose up
```

## E2E Testing
Make a call to the device api with client certificate auth
```bash
curl -ik --cert <CLIENT CERT> --key <CLIENT KEY> \
     --resolve device.catfood.dog:443:0.0.0.0 \
     https://device.catfood.dog/api/v1/device
```
