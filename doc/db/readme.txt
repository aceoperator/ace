# initialize configuration and database
docker run --name db -e MYSQL_ROOT_PASSWORD=a1b2c3d4 -p 3306:3306 mariadb:5.5.52

export ACE_SQL_HOST=127.0.0.1
export ACE_SQL_ROOT_PASSWORD=a1b2c3d4
export ACE_SQL_USER=ace
export ACE_SQL_PASSWORD=a1b2c3d4
export LOAD_DEMO=true
export ACE_SMTP_USER=
export ACE_SMTP_PASSWORD=
export ACE_SMTP_SERVER=localhost
export RUN_SEED=true

~/git/ace/ace-docker/ace-docker-app/src/main/docker/run.sh ~/git/ace/ace-assembly/src/main/config $HOME ~/git/ace/ace-docker/ace-docker-app/src/main/docker ~/git/ace/ace-docker/ace-docker-app/src/main/docker

# drop configuration and database - so that you can reinit using the above command
mysql -h 127.0.0.1 -u root -pa1b2c3d4 -e "DROP DATABASE webtalk"; rm -rf ~/.ace
