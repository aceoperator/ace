# Base image documentation: https://hub.docker.com/_/tomcat/

# Run the base image
docker run --name tomcat -d -it -p=8081:8080 tomcat:7.0.72-jre8

# Login to the ace-app
docker exec -it tomcat bash

# Build ace-app
cd .
docker build --build-arg ACE_VERSION=ace-2.3.0-SNAPSHOT -t aceoperator/app .

# Run ace-app
docker run --name ace-app -d -it -p=8081:8080 --link mail --link ace-db aceoperator/app:latest 

# Inspect the configuration
docker inspect ace-app



