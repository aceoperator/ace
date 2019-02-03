# ------------------------------------------------------------
# First time setup of the mariadb tables for unit testing
# ------------------------------------------------------------
. ~/git/ace/ace-docker/properties.sh
# to override properties that you don't want others to see
. ~/git/ace/ace-docker/properties-secret.sh

cd ~/git/ace/ace-docker/ace-docker-data ; mvn clean install

cd ~/git/ace/ace-docker/ace-docker-compose ; mvn clean install

docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
        -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml up


# ------------------------------------------------------------
# Create a self-signed certificate for aceoperator
# ------------------------------------------------------------
# create public/private key pair and a self-signed x509 certificate for aceoperator
openssl req -x509 -days 365 -newkey rsa:2048 -keyout ~/certs/aceoperator.key -out ~/certs/aceoperator.crt
cat << EOF > ~/certs/passphrase.sh
#!/bin/sh
echo "a1b2c3d4"
EOF
chmod +x ~/certs/passphrase.sh

# copy a CA certificate for the apache frontend. In real life this will be
# replaced with a CA certificate that signs the the aceoperator certificate (see above) 
cp /etc/pki/tls/certs/ca-bundle.crt ~/certs/ca.crt

# create a trust store
cd ~/certs
rm -f roots.pem truststore
wget https://pki.goog/roots.pem
# wget --no-check-certificate https://pki.goog/roots.pem
keytool -import -trustcacerts -alias root -file roots.pem -keystore truststore
# enter password a1b2c3d4
# Add Google recaptcha server certificate
keytool -printcert -rfc -sslserver www.google.com/recaptcha/api/siteverify > recaptcha.pem
# view the certificate to see when it expires and other information
cat recaptcha.pem | keytool -printcert
keytool -importcert --alias google-api -file recaptcha.pem -–keystore truststore
keytool -list --keystore truststore 
# Import other certificates for client sites if needed

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
sudo rm -rf $ACEOPERATOR_HOME/*

# ------------------------------------------------------------
# Start Ace Operator
# ------------------------------------------------------------
. ~/git/ace/ace-docker/properties.sh
. ~/git/ace/ace-docker/properties-secret.sh

docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/app-compose.yml up

# ------------------------------------------------------------
# Stop Ace Operator gracefully
# ------------------------------------------------------------
docker-compose -p aceoperator -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/db-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/data-compose.yml \
    -f ~/git/ace/ace-docker/ace-docker-compose/target/docker-compose/app-compose.yml stop -t 60

# ------------------------------------------------------------
# Backup database
# ------------------------------------------------------------
docker exec ace-data /usr/share/aceoperator/bin/backup.sh [NUM_BACKUP_TO_KEEP=5]

# ------------------------------------------------------------
# Publish to Docker Hub
# ------------------------------------------------------------
export DOCKER_ID_USER="username"
docker login
docker publish imagename:tag
