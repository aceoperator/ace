# Cleanup all the images
docker rmi `docker images | grep -v REPOSITORY | awk '{print $1":"$2'} | grep quik`

# To publish to Docker Hub:
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag

# Run aceoperator docker containers
export ACEOPERATOR_HOME=$HOME
export ACE_SQL_HOST=db
export ACE_SQL_ROOT_PASSWORD=a1b2c3d4
export ACE_SQL_USER=ace
export ACE_SQL_PASSWORD=a1b2c3d4
export LOAD_DEMO=true
export ACE_SMTP_USER=
export ACE_SMTP_PASSWORD=
export ACE_SMTP_SERVER=mail
export RUN_SEED=true

docker-compose -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/aceoperator-compose.yml up