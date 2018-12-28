#!/bin/sh

kubectl delete service aceoperatordb-ext
kubectl delete service aceoperatordb
kubectl delete deployment aceoperatordb
kubectl delete secret aceoperator-certs
kubectl delete secret aceoperator
kubectl delete configmap aceoperator
kubectl delete pvc aceoperator

echo "Here is how the deployment looks so far:"
kubectl get all | grep "$instance" | sed '/^$/d'