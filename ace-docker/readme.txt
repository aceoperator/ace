https://hub.docker.com/r/luxifer/docker-postfix-dovecot/

docker pull luxifer/docker-postfix-dovecot

docker run --name ace-mail -d -p=26:25/tcp -p=144:143/tcp -p=994:993/tcp luxifer/docker-postfix-dovecot:latest

docker exec -it mail bash