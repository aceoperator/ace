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
sudo iptables -A PREROUTING -t nat -i ens33 -p tcp --dport 80 -j DNAT --to $(minikube ip):80
sudo iptables -A FORWARD -p tcp --sport 80 -d $(minikube ip) --dport 80 -j ACCEPT
sudo iptables -A PREROUTING -t nat -i ens33 -p tcp --dport 443 -j DNAT --to $(minikube ip):443
sudo iptables -A FORWARD -p tcp --sport 80 -d $(minikube ip) --dport 443 -j ACCEPT
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

# -------------------------------------------------------------------------------------------
# 1. Preparation
# -------------------------------------------------------------------------------------------
# create NFS mount 
kubectl apply -f $(~/git/ace/ace-kube/src/main/kube/bin/aceoperator-pv.sh)
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
~/git/ace/ace-kube/src/main/kube/bin/deploy_framework.sh ~/git/ace/ace-kube/src/main/kube

# -------------------------------------------------------------------------------------------
3. Create an instance
# -------------------------------------------------------------------------------------------
export INSTANCE=<instance_name>

export ACEOPERATOR_SQL_PASSWORD=a1b2c3d4
export ACEOPERATOR_ADMIN_PASSWORD=a1b2c3d4

~/git/ace/ace-kube/src/main/kube/bin/deploy_instance.sh ~/git/ace/ace-kube/src/main/kube $INSTANCE

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

# ************************************************************----------------------------------------
# cleanup
# ************************************************************----------------------------------------
kubectl config set-context $(kubectl config current-context) --namespace=default
kubectl delete namespace aceoperator
kubectl delete pv aceoperator

minikube ssh
    sudo rm -rf  /var/vol/aceoperator/
    exit

sudo rm -rf /var/vol/aceoperator/instance/*
#done
