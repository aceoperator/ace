# Base image documentation: https://hub.docker.com/_/mariadb/

# Run the base image
docker run --name my-mariadb -e MYSQL_ROOT_PASSWORD=durhamnc -d mariadb:5.5.52

# Build ace-db
cd .
docker build --build-arg ACE_VERSION=ace-2.3.0-SNAPSHOT -t aceoperator/db .

# Run ace-db (-d = detached)
docker run --name ace-db -e MYSQL_ROOT_PASSWORD=durhamnc -d -p=3307:3306/tcp aceoperator/db:latest

# Inspect the configuration
docker inspect ace-db

# Login to the ace-db
docker exec -it ace-db bash