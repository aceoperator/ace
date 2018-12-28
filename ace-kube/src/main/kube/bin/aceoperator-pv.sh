#!/bin/sh

tmpfile=/tmp/aceoperator-pv.yml
cat << EOF > $tmpfile
apiVersion: v1
kind: PersistentVolume
metadata:
  name: aceoperator
spec:
  capacity:
    storage: 5Gi
  accessModes:
  - ReadWriteMany
  nfs:
    # The server address is the Minikube gateway to the host. This way
    # not only the container IP will be visible by the NFS server on the host machine,
    # but the IP address of the `minikube ip` command. You will need to
    # grant access to the `minikube ip` IP address.
    server: $(minikube ip | awk -F '.' '{print $1"."$2"."$3".1"}')
    path: '/var/vol/aceoperator'
EOF
echo $tmpfile
