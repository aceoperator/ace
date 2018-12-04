# start minikube
minikube start --vm-driver kvm2

# to add local images to minikube
eval $(minikube docker-env)
# build the image

# to ssh into minikube vm
minikube ssh

# to delete deployments
kubectl get deployments --all-namespaces
kubectl delete -n NAMESPACE deployment DEPLOYMENT

# stop minikube
minikube stop

---------------------------------------------
# create aceoperator namespace
kubectl create namespace aceoperator
kubectl get namespaces

# create secret
kubectl create -f ./aceoperator-secrets.yml -n aceoperator
kubectl get secrets -n aceoperator

# delete secret
kubectl delete secret aceoperator-secrets -n aceoperator

# create aceoperator pod
kubectl create -f aceoperator-kube.yml -n aceoperator
kubectl get pods -n aceoperator
kubectl describe pod/aceoperator -n aceoperator

# bash into the pod
kubectl exec -n aceoperator -it aceoperator bash

# port forwarding
kubectl port-forward aceoperator 3306:3306 -n aceoperator

# delete pod
kubectl delete pod aceoperator -n aceoperator