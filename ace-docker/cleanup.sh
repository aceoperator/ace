#!/bin/sh

docker rmi aceoperator/frontend:latest aceoperator/frontend:2.3.0-SNAPSHOT \
	aceoperator/app:latest aceoperator/app:2.3.0-SNAPSHOT \
	aceoperator/mail:latest aceoperator/mail:2.3.0-SNAPSHOT \
	aceoperator/db:latest aceoperator/db:2.3.0-SNAPSHOT
	
docker images