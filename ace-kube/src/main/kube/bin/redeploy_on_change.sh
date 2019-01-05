#!/bin/sh

kube_home="$1"

bin_dir="$kube_home/bin"

instances=$(kubectl get deployment --selector=app=aceoperator | grep -v 'NAME' | awk '{print $1}')

for instance in $instances ; do $bin_dir/deploy_instance.sh $kube_home $instance deployment; done 
