# Cleanup all the images
docker rmi `docker images | grep -v REPOSITORY | awk '{print $1":"$2'} | grep quik`

# To publish to Docker Hub:
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag