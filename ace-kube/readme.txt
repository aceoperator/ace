# -------------------------------------------------------
# Preparation
# -------------------------------------------------------

# 1. create persistent volume on the VM
sudo mkdir -p /var/vol/aceoperator/mariadb /var/vol/aceoperator/instance/webtalk

# FIXME - perform fine-grained permissions using group permissions
sudo chmod -R 777 /var/vol/aceoperator

sudo sh -c 'echo  "/var/vol/aceoperator *(rw,root_squash)" > /etc/exports'
sudo exportfs -a

# 2. create private/public key
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=aceoperator.net"

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

# delete deployments
kubectl get deployments --all-namespaces
kubectl delete -n NAMESPACE deployment DEPLOYMENT

# enable ingress controller
minikube addons enable ingress

# stop minikube
minikube stop

# -------------------------------------------------------
# Setup aceoperator service for instance webtalk
# -------------------------------------------------------

# create aceoperator namespace
kubectl create namespace aceoperator

# create secret
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-secrets.yml 

# create NFS mount 
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-nfs.yml
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-nfs-claim.yml
kubectl -n aceoperator get pv
kubectl -n aceoperator get pvc

# create aceoperatordb pod
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-deployment.yml 

# create aceoperatordb service
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service.yml

# create secret for instance webtalk
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-secrets.yml

# create aceoperator deployment
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-deployment.yml

# create a service
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-service.yml

# import certificate
kubectl -n aceoperator create secret tls aceoperator-certs --key ~/certs/kube.key --cert ~/certs/kube.crt 

# create webtalk ingress
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-ingress.yml
