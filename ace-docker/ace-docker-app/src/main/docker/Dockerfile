# Dockerfile for ace-app
FROM tomcat:7.0.72-jre8
LABEL MAINTAINER QUIK Computing <info@quik-j.com>

RUN apt-get update; apt-get -y install nmap; ln -s /usr/bin/ncat /usr/bin/nc

COPY work/common/scripts/*.sh /usr/share/aceoperator/bin/

# blow up apps included with default tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

COPY *.sh /usr/share/aceoperator/bin/
RUN chmod +x /usr/share/aceoperator/bin/*.sh

COPY work/webapps /usr/local/tomcat/webapps
COPY server.xml /usr/local/tomcat/conf

EXPOSE 6970

CMD ["/usr/share/aceoperator/bin/run.sh"]