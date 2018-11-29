# Dockerfile for ace-app
FROM luxifer/docker-postfix-dovecot:latest
MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

COPY main.cf /etc/postfix/

RUN useradd -m  -p `openssl passwd a1b2c3d4` becky; \
	useradd -m  -p `openssl passwd a1b2c3d4` amit;  \
	useradd -m  -p `openssl passwd a1b2c3d4` operations