# Dockerfile for ace-db
FROM httpd:latest
LABEL MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

ARG ACE_APP_HOST=app
ENV ACE_APP_HOST $ACE_APP_HOST

COPY * /usr/local/apache2/conf/
RUN chmod +x /usr/local/apache2/conf/passphrase.sh
