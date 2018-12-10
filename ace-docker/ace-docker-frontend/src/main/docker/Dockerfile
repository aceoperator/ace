# Dockerfile for ace-db
FROM httpd:latest
LABEL MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

ARG ACEOPERATOR_APP_HOST=app
ENV ACEOPERATOR_APP_HOST $ACEOPERATOR_APP_HOST

ARG ACEOPERATOR_CERTS_DIR=/etc/pki/tls/certs
ENV ACEOPERATOR_CERTS_DIR $ACEOPERATOR_CERTS_DIR

COPY httpd.conf /usr/local/apache2/conf/
