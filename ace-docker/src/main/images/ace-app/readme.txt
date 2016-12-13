# Base image documentation: https://hub.docker.com/_/tomcat/

# Run the base image
docker run --name tomcat -d -it -p=8081:8080/tcp -p=8443:8443/tcp tomcat:7.0.72-jre8

# Run ace-app
docker run --name ace-app -d -it -p=8080:8080/tcp -p=8443:8443/tcp --link ace-mail:mail --link ace-db:db aceoperator/app:latest 

# Login to the ace-app
docker exec -it ace-app bash

# Inspect the configuration
docker inspect ace-app