# ------------------------------------------------------------
# First time setup of the mariadb tables for unit testing
# ------------------------------------------------------------
. ~/git/ace/ace-docker/properties.sh
. ~/git/ace/ace-docker/properties-secret.sh

cd ~/git/ace/ace-docker/ace-docker-data ; mvn clean install

cd ~/git/ace/ace-docker/ace-docker-compose ; mvn clean install

docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
        -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml up
# ------------------------------------------------------------
# Cleanup all 
# ------------------------------------------------------------
docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/app-compose.yml down

docker rmi `docker images | grep -v REPOSITORY | awk '{print $1":"$2'} | grep quik`

# Remove the database tablespace
docker volume rm $(docker volume ls -qf dangling=true | grep -v VOLUME)

# Remove .ace
sudo rm -rf $ACEOPERATOR_HOME/.ace

# ------------------------------------------------------------
# Start Ace Operator
# ------------------------------------------------------------
. ~/git/ace/ace-docker/properties.sh
. ~/git/ace/ace-docker/properties-secret.sh

docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/app-compose.yml up

# ------------------------------------------------------------
# Publish to Docker Hub
# ------------------------------------------------------------
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag
