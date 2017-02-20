#!/bin/sh

docker rmi `docker images | grep aceoperator | awk '{print $1":"$2}'`
	
docker images