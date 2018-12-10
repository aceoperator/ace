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
kubectl get namespaces

# create secret
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-secrets.yml -n aceoperator
kubectl get secrets -n aceoperator

# create aceoperator pod
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-pod.yml -n aceoperator
kubectl get pods -n aceoperator
kubectl describe pod/aceoperator -n aceoperator

# create a service
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-service.yml -n aceoperator

# import certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ~/certs/kube.key -out ~/certs/kube.crt -subj "/CN=aceoperator.net"
kubectl create secret tls aceoperator-certs --key ~/certs/kube.key --cert ~/certs/kube.crt -n aceoperator

# create an ingress
kubectl create -f ~/git/ace/ace-kube/src/main/kube/aceoperator-ingress.yml -n aceoperator

# delete secret
kubectl delete secret aceoperator-secrets -n aceoperator

# delete pod
kubectl delete pod aceoperator -n aceoperator

# delete namespace
kubectl delete namespace aceoperator

# delete all in a namespace
kubectl delete --all -n aceoperator
-----------------------------------------------------
# bash into the pod
kubectl exec -n aceoperator -it aceoperator bash

# port forwarding
kubectl port-forward aceoperator 8080:8080 -n aceoperator
