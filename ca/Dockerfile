FROM alpine:latest

RUN apk add --no-cache \
	openssl gettext bash

COPY bin/ /
RUN mkdir /configs
COPY config/ /configs
