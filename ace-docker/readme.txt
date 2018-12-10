# First time setup of the Oracle tablespace for testing
cd ~/git/ace/ace-docker/ace-docker-data ; mvn clean install
cd ~/git/ace/ace-docker/ace-docker-compose ; mvn clean install

# Add the environment variables described below

docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
        -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml up

# Cleanup all the images
docker rmi `docker images | grep -v REPOSITORY | awk '{print $1":"$2'} | grep quik`

# Remove the database tablespace
docker volume rm $(docker volume ls -qf dangling=true | grep -v VOLUME)

# Run aceoperator docker containers
export ACEOPERATOR_HOME=$HOME
export ACEOPERATOR_SQL_HOST=db
export ACEOPERATOR_SQL_ROOT_PASSWORD=a1b2c3d4
export ACEOPERATOR_SQL_USER=ace
export ACEOPERATOR_SQL_PASSWORD=a1b2c3d4
export ACEOPERATOR_SQL_DB=webtalk
export ACEOPERATOR_SQL_PORT=3306
export ACEOPERATOR_SMTP_USER=
export ACEOPERATOR_SMTP_PASSWORD=
export ACEOPERATOR_SMTP_SERVER=mail
export ACEOPERATOR_DATA_HOST=data
export ACEOPERATOR_APP_HOST=app
export ACEOPERATOR_CERTS_DIR=$HOME/certs
export LOAD_DEMO=true
export RUN_SEED=true


docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/mail-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/app-compose.yml up

# To publish to Docker Hub:
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag
