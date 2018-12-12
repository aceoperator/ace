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
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-secrets.yml -n aceoperator

# create aceoperatordb pod
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-pod.yml -n aceoperator

# crete aceoperatordb service
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperatordb-service.yml -n aceoperator

# use the command below to view the allocated port of the NodePort and connect to the database
kubectl get services -n aceoperator
mysql -h $(minikube ip) -P <PORTNUM> -u root -p

# create secret for instance webtalk
kubectl create -f ~/git/ace/ace-kube/src/main/kube/webtalk-secrets.yml -n aceoperator

# create aceoperator pod
kubectl create -f ~/git/ace/ace-kube/src/main/kube/webtalk-pod.yml -n aceoperator

# create a service
kubectl create -f ~/git/ace/ace-kube/src/main/kube/webtalk-service.yml -n aceoperator

# import certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=aceoperator.net"
kubectl create secret tls aceoperator-certs --key ~/certs/kube.key --cert ~/certs/kube.crt -n aceoperator

# create webtalk ingress
kubectl create -f ~/git/ace/ace-kube/src/main/kube/webtalk-ingress.yml -n aceoperator


