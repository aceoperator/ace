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

echo "Here is how the deployment looks so far:"
kubectl get all | grep "$instance" | sed '/^$/d'