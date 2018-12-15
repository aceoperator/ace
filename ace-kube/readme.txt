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
sudo iptables -A FORWARD -p tcp -d $(minikube ip) --dport 80 -j ACCEPT
sudo iptables -A PREROUTING -t nat -i ens33 -p tcp --dport 443 -j DNAT --to $(minikube ip):443
sudo iptables -A FORWARD -p tcp -d $(minikube ip) --dport 443 -j ACCEPT
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

# create aceoperator namespace
kubectl create namespace aceoperator

# create secret
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-secrets.yml 

# create NFS mount 
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-pv.yml
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-pvc.yml
kubectl -n aceoperator get pv
kubectl -n aceoperator get pvc

# create aceoperatordb pod
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-deployment.yml 

# create aceoperatordb service
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service.yml

# service to access mysql externally - DON'T DOIT in production environment
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service-ext.yml 

# create configmap for aceoperator
kubectl -n aceoperator create configmap aceoperator --from-env-file=$HOME/git/ace/ace-kube/src/main/kube/aceoperator.properties

# -------------------------------------------------------------------------------------------

# create secret for instance webtalk
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-secrets.yml

# create configmap for webtalk
kubectl -n aceoperator create configmap webtalk --from-env-file=$HOME/git/ace/ace-kube/src/main/kube/webtalk.properties

# create webtalk deployment
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-deployment.yml

# create a service
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-service.yml

# import certificate
kubectl -n aceoperator create secret tls aceoperator-certs --key ~/certs/kube.key --cert ~/certs/kube.crt 

# create webtalk ingress
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-ingress.yml

# debug tools for certificate and stuff
openssl s_client -host webtalk.aceoperator.net -port 443
curl -I -k -v --resolve webtalk.aceoperator.net https://webtalk.aceoperator.net/ace-contactcenter

# -----------------------------------------------------------------------------------------------
# cleanup

kubectl delete namespace aceoperator
kubectl delete pv aceoperator

sudo rm -rf /var/vol/aceoperator/instance/webtalk/.ace