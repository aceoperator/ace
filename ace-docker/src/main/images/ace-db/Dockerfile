# Dockerfile for ace-db
FROM mariadb:5.5.52
LABEL MAINTAINER QUIK Computing <info@quik-j.com>

ARG MYSQL_ROOT_PASSWORD=a1b2c3d4
ENV MYSQL_ROOT_PASSWORD $MYSQL_ROOT_PASSWORD

ARG MYSQL_DATABASE=webtalk
ENV MYSQL_DATABASE $MYSQL_DATABASE

ARG MYSQL_USER=ace
ENV MYSQL_USER $MYSQL_USER

ARG MYSQL_PASSWORD=a1b2c3d4
ENV MYSQL_PASSWORD $MYSQL_PASSWORD 

# COPY work/init_objects.sql /docker-entrypoint-initdb.d/
# COPY init_demo_users.sql /docker-entrypoint-initdb.d/
