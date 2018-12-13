# Create NFS mount on linux
sudo mkdir /var/nfs/mariadb
#FIXME
sudo 777 /var/nfs/mariadb
sudo sh -c 'echo  "/var/nfs *(rw,root_squash)" > /etc/exports'
sudo exportfs -a

------------------------------------------
# start minikube
minikube start --vm-driver kvm2

# to add local images to minikube
eval $(minikube docker-env)
# build the image

# to ssh into minikube vm
minikube ssh

# Remove unused docker imaages from minikube
minikube ssh
docker container rm $(docker ps -a -q)
# docker rmi to remove the image

# to delete deployments
kubectl get deployments --all-namespaces
kubectl delete -n NAMESPACE deployment DEPLOYMENT

# add ingress controller
minikube addons enable ingress

# stop minikube
minikube stop

---------------------------------------------
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
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-pod.yml 

# crete aceoperatordb service
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service.yml 

# use the command below to view the allocated port of the NodePort and connect to the database
kubectl get services -n aceoperator
mysql -h $(minikube ip) -P <PORTNUM> -u root -p


# create secret for instance webtalk
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-secrets.yml

# create aceoperator pod
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-pod.yml

# create a service
kubectl  -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-service.yml

# import certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=aceoperator.net"
kubectl -n aceoperator create secret tls aceoperator-certs --key ~/certs/kube.key --cert ~/certs/kube.crt 

# create webtalk ingress
kubectl -n aceoperator create -f ~/git/ace/ace-kube/src/main/kube/webtalk-ingress.yml 


