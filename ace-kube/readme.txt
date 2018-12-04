# start minikube
minikube start --vm-driver kvm2

# to use local images
eval $(minikube docker-env)

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
kubectl create -f ./aceoperator-secrets.yml --namespace=aceoperator
kubectl get secrets --namespace=aceoperator

# create aceoperator pod
kubectl create -f aceoperator-kube.yml --namespace=aceoperator
kubectl get pods --namespace=aceoperator

# port forwarding
kubectl port-forward aceoperator 3306:3306 --namespace=aceoperator

# delete pod
kubectl delete pod aceoperator --namespace=aceoperator