#!/bin/sh

instance=webtalk
if [ -n "$1" ]; then
    instance=$1
fi

prop='data'
data="["

element=$(kubectl get secret aceoperator -o json |  jq '.["data"]')
data="${data}\n${element}"

element=$(kubectl get configmap aceoperator -o json |  jq '.["data"]')
data="${data},\n${element}"

element=$(kubectl get secret $instance -o json |  jq '.["data"]')
data="${data},\n${element}"

element=$(kubectl get configmap $instance -o json |  jq '.["data"]')
data="${data},\n${element}"

data="${data}\n]"

# echo -e $data
echo -e $data | md5sum | awk '{print $1}'