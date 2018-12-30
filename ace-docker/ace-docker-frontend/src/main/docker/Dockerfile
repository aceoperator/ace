# Dockerfile for ace-db
FROM httpd:latest
LABEL MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

ARG ACE3_FE_APP_HOST=app
ENV ACE3_FE_APP_HOST $ACE3_FE_APP_HOST

ARG ACE3_FE_CERTS_DIR=/etc/pki/tls/certs
ENV ACE3_FE_CERTS_DIR $ACE3_FE_CERTS_DIR

COPY httpd.conf /usr/local/apache2/conf/
