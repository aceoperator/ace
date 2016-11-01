# Dockerfile for ace-app
FROM tomcat:7.0.72-jre8
MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

ARG ACE_VERSION=ace-2.3.0-SNAPSHOT

COPY work/${ACE_VERSION}/${ACE_VERSION}/.ace /usr/share/aceoperator/.ace
RUN sed -i -e 's/\$\$ACE(ACE_SQL_USER)/ace/g; s/\$\$ACE(ACE_SQL_PASSWORD)/a1b2c3d4/g;s/\$\$ACE(ACE_SQL_HOST)/db/g' \
	 /usr/share/aceoperator/.ace/config/log4j.properties; \
	sed -i -e 's/\$\$ACE(ACE_SQL_USER)/ace/g; s/\$\$ACE(ACE_SQL_PASSWORD)/a1b2c3d4/g; s/localhost/db/g' \
	 /usr/share/aceoperator/.ace/config/properties/ace-db.properties; \
	sed -i -e 's/\$\$ACE(ACE_SMTP_USER)//g; s/\$\$ACE(ACE_SMTP_PASSWORD)//g; s/\$\$ACE(ACE_SMTP_SERVER)/mail/g' \
		/usr/share/aceoperator/.ace/config/properties/mail.properties

RUN rm -rf /usr/local/tomcat/webapps/*

COPY work/${ACE_VERSION}/${ACE_VERSION}/web/ace-communicator.war /usr/local/tomcat/webapps/
COPY work/${ACE_VERSION}/${ACE_VERSION}/web/ace-contactcenter.war /usr/local/tomcat/webapps/
COPY work/${ACE_VERSION}/${ACE_VERSION}/web/ace-custom /usr/local/tomcat/webapps/ace-custom

RUN sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF;operations@acedemo.net/' \
	/usr/share/aceoperator/.ace/profiles/default-operator.properties 

COPY server.xml /usr/local/tomcat/conf
COPY .keystore /root/

ENV JAVA_OPTS "-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8" -Duser.timezone=US/Eastern -Dace.root=/usr/share/aceoperator"

EXPOSE 8443

VOLUME ["/usr/local/tomcat/webapps/.ace"]