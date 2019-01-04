# ************************************************************
# Preparation
# ************************************************************

# 1. create persistent volume on the VM using a NFS mount
sudo mkdir -p /var/vol/aceoperator/instance/webtalk

sudo chown -R nfsnobody.nfsnobody /var/vol/aceoperator

sudo sh -c 'echo  "/var/vol/aceoperator *(rw,root_squash)" > /etc/exports'
sudo exportfs -a

# 2. create private/public key for use by the ingress
mkdir ~/certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=*.aceoperator.net"

# port forward http to minikube (work in progress)
sudo echo 1 > /proc/sys/net/ipv4/ip_forward

sudo iptables -I INPUT 1 -p tcp --dport 80 -j LOG
sudo iptables -I INPUT 1 -p tcp --dport 443 -j LOG
sudo iptables -t nat -p tcp -I PREROUTING 1  -j LOG
sudo iptables -t nat -p tcp -I POSTROUTING 1 -j LOG

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
# build the image using mvn command

# remove unused docker imaages from minikube
minikube ssh
    docker container rm $(docker ps -a -q)
    # docker rmi to remove the image
    exit

# delete deployments
kubectl get deployments --all-namespaces
kubectl delete -n NAMESPACE deployment DEPLOYMENT

# enable ingress controller
minikube addons enable ingress

# bring up dashboard
minikube dashboard

# stop minikube
minikube stop

# ************************************************************
# Setup aceoperator service and an instance
# ************************************************************
# Add to .bashrc/.bash_profile
export ACE3_HOME=~/git/ace/ace-kube/src/main/kube
export ACE3_BIN=$ACE3_HOME/bin
export PATH=$PATH:$ACE3_BIN
# -------------------------------------------------------------------------------------------
# 1. Preparation
# -------------------------------------------------------------------------------------------
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
export ACEOPERATOR_SQL_ROOT_PASSWORD=a1b2c3d4
export ACEOPERATOR_SMTP_PASSWORD=
export ACEOPERATOR_RECAPTCHA_SECRET=
deploy_framework.sh $ACE3_HOME

# -------------------------------------------------------------------------------------------
3. Create an instance
# -------------------------------------------------------------------------------------------
export INSTANCE=<instance_name>

export ACEOPERATOR_SQL_PASSWORD=a1b2c3d4
export ACEOPERATOR_ADMIN_PASSWORD=a1b2c3d4
export ACE3_DATA_USERS=TODO

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
kubectl logs $(kubectl get pod | grep ${INSTANCE} | awk '{print $1}') -c ace-data

# execute command on a container
kubectl exec -it $(kubectl get pod | grep ${INSTANCE} | awk '{print $1}') -c ace-data -- /bin/bash

# To test the nfs mount

# ************************************************************----------------------------------------
# cleanup
# ************************************************************----------------------------------------
kubectl config set-context $(kubectl config current-context) --namespace=default
kubectl delete namespace aceoperator
kubectl delete pv aceoperator

minikube ssh "sudo rm -rf  /var/vol/aceoperator/"

sudo rm -rf /var/vol/aceoperator/instance/*
#done
