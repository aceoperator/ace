# Dockerfile for ace-app
FROM tomcat:7.0.72-jre8
LABEL MAINTAINER QUIK Computing <info@quik-j.com>

ARG ACEOPERATOR_DATA_HOST=db
ENV ACEOPERATOR_DATA_HOST $ACEOPERATOR_DATA_HOST

RUN apt-get update; apt-get -y install nmap

COPY work/common/scripts/*.sh /usr/share/aceoperator/bin/

# blow up apps included with default tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

COPY *.sh /usr/share/aceoperator/bin/
RUN chmod +x /usr/share/aceoperator/bin/*.sh

COPY work/webapps /usr/local/tomcat/webapps
COPY server.xml /usr/local/tomcat/conf

EXPOSE 6970 8080 8443 8009

CMD ["/usr/share/aceoperator/bin/run.sh"]