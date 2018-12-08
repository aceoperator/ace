# Dockerfile for ace-db
FROM httpd:latest
LABEL MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

ARG ACEOPERATOR_APP_HOST=app
ENV ACEOPERATOR_APP_HOST $ACEOPERATOR_APP_HOST

COPY * /usr/local/apache2/conf/
RUN chmod +x /usr/local/apache2/conf/passphrase.sh
