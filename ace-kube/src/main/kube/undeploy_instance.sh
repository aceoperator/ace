#!/bin/sh

instance=webtalk
if [ -n "$1" ]; then
    instance="$1"
fi

echo "Going to undeploy $instance"

kubectl delete ingress "$instance"
kubectl delete service "$instance"
kubectl delete deployment "$instance"
kubectl delete configmap "$instance"
kubectl delete secret "$instance"

kubectl get all | grep "$instance"