# Dockerfile for ace-app
FROM ubuntu:disco
LABEL MAINTAINER QUIK Computing <info@quik-j.com>

ARG ACE_SQL_HOST=db
ENV ACE_SQL_HOST $ACE_SQL_HOST

ARG ACE_SQL_ROOT_PASSWORD=a1b2c3d4
ENV ACE_SQL_ROOT_PASSWORD $ACE_SQL_ROOT_PASSWORD

ARG ACE_SQL_USER=ace
ENV ACE_SQL_USER $ACE_SQL_USER

ARG ACE_SQL_PASSWORD=a1b2c3d4
ENV ACE_SQL_PASSWORD $ACE_SQL_PASSWORD

ARG ACE_SMTP_SERVER=mail
ENV ACE_SMTP_SERVER $ACE_SMTP_SERVER

ARG ACE_SMTP_USER=
ENV ACE_SMTP_USER $ACE_SMTP_USER

ARG ACE_SMTP_PASSWORD=
ENV ACE_SMTP_PASSWORD $ACE_SMTP_PASSWORD

ARG LOAD_DEMO=true
ENV LOAD_DEMO $LOAD_DEMO

ARG RUN_SEED=true
ENV RUN_SEED $RUN_SEED

RUN apt-get update; apt-get -y install mariadb-client netcat

COPY *.sh /usr/share/aceoperator/bin/
RUN chmod +x /usr/share/aceoperator/bin/*.sh

COPY work/.ace /usr/share/aceoperator/.ace
COPY *.sql /usr/share/aceoperator/.ace/sql/

COPY config-override.xml /usr/share/aceoperator/.ace/spring/ace-app/

CMD ["/usr/share/aceoperator/bin/populate.sh"]