# -------------------------------------------------------
# Preparation
# -------------------------------------------------------

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

# -------------------------------------------------------
# Minikube operations
# -------------------------------------------------------

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

# -------------------------------------------------------
# Setup aceoperator service for instance webtalk
# -------------------------------------------------------
# create NFS mount 
kubectl apply -f $(~/git/ace/ace-kube/src/main/kube/aceoperator-pv.sh)
kubectl get pv

# create aceoperator namespace
kubectl create namespace aceoperator
kubectl config set-context $(kubectl config current-context) --namespace=aceoperator
kubectl config view | grep namespace:

# create secret for aceoperator
export ACEOPERATOR_SQL_ROOT_PASSWORD=a1b2c3d4
export ACEOPERATOR_SMTP_PASSWORD=
export ACEOPERATOR_RECAPTCHA_SECRET=
~/git/ace/ace-kube/src/main/kube/aceoperator-secret-from-env.sh | kubectl apply -f -

# create configmap for aceoperator
kubectl create configmap aceoperator --from-env-file=$HOME/git/ace/ace-kube/src/main/kube/aceoperator.properties

# create a persistent volume claim
kubectl apply -f ~/git/ace/ace-kube/src/main/kube/aceoperator-pvc.yml
kubectl  get pvc

# create aceoperatordb deployment
kubectl apply -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-deployment.yml 

# create aceoperatordb service
kubectl apply -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service.yml

# service to access mysql externally - DON'T DOIT in production environment
kubectl apply -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service-ext.yml 

# import certificate
kubectl create secret tls aceoperator-certs --save-config --key ~/certs/kube.key --cert ~/certs/kube.crt 

# -------------------------------------------------------------------------------------------
export ACEOPERATOR_SQL_PASSWORD=a1b2c3d4
export ACEOPERATOR_ADMIN_PASSWORD=a1b2c3d4

~/git/ace/ace-kube/src/main/kube/deploy_instance.sh ~/git/ace/ace-kube/src/main/kube ~/git/ace/ace-kube/src/main/kube webtalk

# ----------------------------------------------------------------------------------------------
# Debug tools
# -----------------------------------------------------------------------------------------------
# test mariadb connectivity
mysqladmin ping  -h $(minikube ip) -u root -p \
    -P $(kubectl get service | grep '^aceoperatordb-ext' | awk '{print $5}' | awk -F ':' '{print substr($2, 1, length($2) - 4)}')
mysql -h $(minikube ip) -u root -p \
    -P $(kubectl get service | grep '^aceoperatordb-ext' | awk '{print $5}' | awk -F ':' '{print substr($2, 1, length($2) - 4)}')

# verify ingress operation
openssl s_client -host webtalk.aceoperator.net -port 443
curl -I -k -v --resolve webtalk.aceoperator.net https://webtalk.aceoperator.net/ace-contactcenter

# view logs for a container (replace webtalk with the deployment name and ace-data with the container name)
kubectl logs $(kubectl get pod | grep webtalk | awk '{print $1}') -c ace-data

# execute command on a container (replace webtalk with the deployment name and ace-data with the container name and /bin/bash with the command name)
kubectl exec -it $(kubectl get pod | grep webtalk | awk '{print $1}') -c ace-data -- /bin/bash

# -----------------------------------------------------------------------------------------------
# cleanup
kubectl config set-context $(kubectl config current-context) --namespace=default
kubectl delete namespace aceoperator
kubectl delete pv aceoperator

minikube ssh
    sudo rm -rf  /var/vol/aceoperator/
    exit

sudo rm -rf /var/vol/aceoperator/instance/*
#done
