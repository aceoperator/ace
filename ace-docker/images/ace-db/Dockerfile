# Dockerfile for ace-db
FROM mariadb:5.5.52
MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

COPY work/init_ace.sql /docker-entrypoint-initdb.d/
RUN sed -i -e 's/\$\$ACE(ACE_SQL_USER)/ace/g; s/\$\$ACE(ACE_SQL_PASSWORD)/a1b2c3d4/g' /docker-entrypoint-initdb.d/init_ace.sql
COPY init_default_users.sql /docker-entrypoint-initdb.d/
