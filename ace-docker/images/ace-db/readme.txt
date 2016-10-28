# Base image documentation: https://hub.docker.com/_/mariadb/

# Run the base image
docker run --name my-mariadb -e MYSQL_ROOT_PASSWORD=durhamnc -d mariadb:5.5.52

# Run ace-db (-d = detached)
docker run --name ace-db -e MYSQL_ROOT_PASSWORD=a1b2c3d4 -d -p=3306:3306/tcp aceoperator/db:latest

# Inspect the configuration
docker inspect ace-db

# Login to the ace-db
docker exec -it ace-db bash