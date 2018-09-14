#!/bin/sh

docker stop `docker ps  | awk '{print $1}' | grep --invert-match CONTAINER`
docker rm `docker ps -a  | awk '{print $1}' | grep --invert-match CONTAINER`
docker rmi `docker images | grep aceoperator | awk '{print $1":"$2}'`	
docker images