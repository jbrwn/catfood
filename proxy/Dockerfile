FROM nginx:alpine

COPY default.conf.template /etc/nginx/templates/
COPY ./includes /etc/nginx/includes
COPY ssl-dhparams.pem /etc/nginx/ssl/
COPY create-ssl-certs.sh /docker-entrypoint.d/
