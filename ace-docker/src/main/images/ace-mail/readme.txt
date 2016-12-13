# Base image documentation: https://hub.docker.com/r/luxifer/docker-postfix-dovecot/

# Run the base image
docker run --name ace-mail -d -p=25:25/tcp -p=143:143/tcp -p=993:993/tcp luxifer/docker-postfix-dovecot:latest

# Run ace-mail
docker run --name ace-mail -d -it -p=25:25/tcp -p=143:143/tcp -p=993:993/tcp -h acedemo.net aceoperator/mail:latest 

# Login to the ace-mail
docker exec -it ace-mail bash

# Inspect the configuration
docker inspect ace-mail



