# ************************************************************
# Preparation
# ************************************************************

# 1. create persistent volume on the VM using a NFS mount
sudo systemctl disable firewalld
sudo systemctl stop firewalld
sudo systemctl start nfs-server
sudo systemctl enable nfs-server

sudo mkdir -p /var/vol/aceoperator/instance/webtalk

sudo chown -R nfsnobody.nfsnobody /var/vol/aceoperator

sudo sh -c 'echo  "/var/vol/aceoperator *(rw,root_squash)" > /etc/exports'
sudo exportfs -a

# 2. create private/public key for use by the ingress
mkdir ~/certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=*.aceoperator.net"

# 3. install json parser for bash
sudo yum install -y jq

# 4. create a trust store (needed for recaptcha)
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

# port forward http to minikube (work in progress)
KUBEIF='virbr1'
LANIF='ens33'

# enable ip forwarding in the kernel
echo 'Enabling Kernel IP forwarding...'
sudo echo 1 > /proc/sys/net/ipv4/ip_forward

# enable masquerading to allow LAN internet access
echo 'Enabling IP Masquerading and other rules...'
sudo iptables -t nat -A POSTROUTING -o $LANIF -p tcp --dport 80 -j MASQUERADE
sudo iptables -t nat -A POSTROUTING -o $LANIF -p tcp --dport 443 -j MASQUERADE
iptables -A FORWARD -i $LANIF -o $KUBEIF -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -A FORWARD -i $KUBEIF -o $LANIF -j ACCEPT

iptables -t nat -A POSTROUTING -o $KUBEIF -j MASQUERADE
iptables -A FORWARD -i $KUBEIF -o $LANIF -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -A FORWARD -i $LANIF -o $KUBEIF -j ACCEPT

# old
sudo echo 1 > /proc/sys/net/ipv4/ip_forward

sudo iptables -t nat -A PREROUTING -s 0/0 -p tcp -i ens33 --dport 80 -j DNAT --to $(minikube ip):80
sudo iptables -t nat -A PREROUTING -s 0/0 -p tcp -i ens33 --dport 443 -j DNAT --to $(minikube ip):443
sudo iptables -t nat -A POSTROUTING -j MASQUERADE 
sudo iptables -t nat -L -n -v

# ************************************************************
# Minikube operations
# ************************************************************
# start minikube
minikube start --vm-driver kvm2

# ssh into minikube vm
minikube ssh

# add local images to minikube
eval $(minikube docker-env)
cd ~/git/ace/ace-docker
mvn -P docker clean install

# remove unused docker imaages from minikube
eval $(minikube docker-env)
docker container rm $(docker ps -a -q)
docker rmi `docker images | grep -v REPOSITORY | awk '{print $1":"$2'} | grep quik`

# delete deployments
kubectl get deployments --all-namespaces
kubectl delete -n NAMESPACE deployment DEPLOYMENT

# bring up dashboard
minikube dashboard

# stop minikube
minikube stop

# ************************************************************
# Setup aceoperator service and an instance
# ************************************************************
. ~/git/ace/ace-docker/properties.sh
# to override properties that you don't want others to see
. ~/git/ace/ace-docker/properties-secret.sh

# Add to .bashrc/.bash_profile
export ACE3_HOME=~/git/ace/ace-kube/src/main/kube
export ACE3_BIN=$ACE3_HOME/bin
export PATH=$PATH:$ACE3_BIN
# -------------------------------------------------------------------------------------------
# 1. Preparation
# -------------------------------------------------------------------------------------------
# enable ingress controller
minikube addons enable ingress

# create NFS mount 
kubectl apply -f $(aceoperator-pv.sh)
kubectl get pv

# create aceoperator namespace
kubectl create namespace aceoperator
kubectl config set-context $(kubectl config current-context) --namespace=aceoperator
kubectl config view | grep namespace:

# -------------------------------------------------------------------------------------------
# 2. Deploy aceoperator framework and database service
# -------------------------------------------------------------------------------------------
. ~/git/ace/ace-docker/properties.sh
# to override properties that you don't want others to see
. ~/git/ace/ace-docker/properties-secret.sh

deploy_framework.sh $ACE3_HOME

# -------------------------------------------------------------------------------------------
3. Create an instance
# -------------------------------------------------------------------------------------------
export INSTANCE=<instance_name>

# If the instance needs its own keystore, create it in file $ACE3_FE_CERTS_DIR/$INSTANCE-truststore

deploy_instance.sh $ACE3_HOME $INSTANCE
# ----------------------------------------------------------------------------------------------
# 4. Control resources
# ----------------------------------------------------------------------------------------------
# The CPU resource is measured in CPU units. One CPU, in Kubernetes, is equivalent to:
# 1 AWS vCPU
# 1 GCP Core
# 1 Azure vCore
# 1 Hyperthread on a bare-metal Intel processor with Hyperthreading
# Fractional values are allowed. A Container that requests 0.5 CPU is guaranteed half as much CPU as a Container that requests 1 CPU. 
# You can use the suffix m to mean milli. For example 100m CPU, 100 milliCPU, and 0.1 CPU are all the same. 
# Precision finer than 1m is not allowed.

kubectl set resources deployment ${INSTANCE} -c=ace-app --limits=cpu=200m,memory=512Mi

# Edit configmap/secret/etc.
kubectl edit configmap $INSTANCE

# for the change to take effect on an instance
deploy_instance.sh $ACE3_HOME $INSTANCE deployment

# ************************************************************---------------------------------------
# Debugging tools
# ************************************************************----------------------------------------
# test mariadb connectivity
mysqladmin ping  -h $(minikube ip) -u root -p \
    -P $(kubectl get service | grep '^aceoperatordb-ext' | awk '{print $5}' | awk -F ':' '{print substr($2, 1, length($2) - 4)}')
mysql -h $(minikube ip) -u root -p \
    -P $(kubectl get service | grep '^aceoperatordb-ext' | awk '{print $5}' | awk -F ':' '{print substr($2, 1, length($2) - 4)}')

# verify ingress operation
openssl s_client -host ${INSTANCE}.aceoperator.net -port 443
curl -I -k -v --resolve ${INSTANCE}.aceoperator.net https://${INSTANCE}.aceoperator.net/ace-contactcenter

# view logs for a container
kubectl logs -n kube-system $(kubectl -n kube-system get pod | grep nginx-ingress-controller | awk '{print $1}') -f

# view logs on ingress
kubectl logs $(kubectl get pod | grep ${INSTANCE} | awk '{print $1}') -c ace-app


# execute command on a container
kubectl exec -it $(kubectl get pod | grep ${INSTANCE} | awk '{print $1}') -c ace-app -- /bin/bash

# get a list of all deployed instances
kubectl get pod --selector=app=aceoperator -o wide

# test aceoperatordb livensess
    # to view the health of the system
    kubectl describe pod $(kubectl get pod | grep aceoperatordb | awk '{print $1}')
    # and view the events
    # view all system events
    kubectl get events --sort-by=.metadata.creationTimestamp
    # change password using mysqladmin password

# test the nfs mount
minikube ssh
    sudo mkdir -p /mnt/aceoperator
    sudo mount -v -t nfs $(ip -4 addr show dev eth1 | grep inet | awk '{print $2}' | awk -F '/' '{print $1}' | awk -F '.' '{print $1"."$2"."$3".1"}'):/var/vol/aceoperator /mnt/aceoperator
    # And verify
    sudo umount /mnt/aceoperator
# ************************************************************----------------------------------------
# cleanup
# ************************************************************----------------------------------------
kubectl config set-context $(kubectl config current-context) --namespace=default
kubectl delete namespace aceoperator
kubectl delete pv aceoperator

minikube ssh "sudo rm -rf  /var/vol/aceoperator/"

sudo rm -rf /var/vol/aceoperator/instance/*
#done
