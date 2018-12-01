# Dockerfile for ace-app
FROM tomcat:7.0.72-jre8
LABEL MAINTAINER QUIK Computing <info@quik-j.com>

ARG ACE_VERSION=BLAH

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

ARG RUN_TOMCAT=true
ENV RUN_TOMCAT $RUN_TOMCAT

RUN apt-get update; apt-get -y install mariadb-client netcat; rm -rf /usr/local/tomcat/webapps/*

COPY *.sh /usr/share/aceoperator/bin/
RUN chmod +x /usr/share/aceoperator/bin/*.sh

COPY work/${ACE_VERSION}/.ace /usr/share/aceoperator/.ace
COPY *.sql /usr/share/aceoperator/.ace/sql/

COPY work/${ACE_VERSION}/web/*.war /usr/local/tomcat/webapps/
COPY work/${ACE_VERSION}/web/ace-custom /usr/local/tomcat/webapps/ace-custom

COPY server.xml /usr/local/tomcat/conf
COPY config-override.xml /usr/share/aceoperator/.ace/spring/ace-app/

CMD ["/usr/share/aceoperator/bin/run.sh"]