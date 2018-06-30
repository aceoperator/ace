The docker image build is not a part of the aceoperator build. To build:
cd ace-docker
mvn clean install

To publish to Docker Hub:
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag