#!/bin/sh

function prompt_password {
    while true; do
        echo -n "    Enter password: " 
        read -s  password
        echo -e -n "\n    Reenter password: " 
        read -s  password_again
        if [ "$password" = "$password_again" ]; then
            echo -e "\n    Password set"
            break
        else
            echo -e "\n    Passwords did not match. Please try again"
        fi
    done
}

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <KUBE_HOME_DIR> [CERTS_DIR]"
    exit 1
fi

kube_home="$1"

certs_dir=$HOME/certs
if [ -d "$2" ]; then
    certs_dir="$2"
fi

bin_dir="$kube_home/bin"
template_dir="$kube_home/template"
properties_dir="$kube_home/property"

property_file="$properties_dir/aceoperator.properties"
if [ ! -f  "$property_file" ]; then
    echo "The properties file for aceoperator does not exist. Going to bail out"
    exit 1
fi
 echo "Going to deploy aceoperator framework"

# create secret for aceoperator
if [ -z "$ACEOPERATOR_SQL_ROOT_PASSWORD" ]; then
    echo "MYSQL root password is not set using env variable. Going to set one up"
    prompt_password
    export ACEOPERATOR_SQL_ROOT_PASSWORD=$password
fi

if [ -z "$ACEOPERATOR_SMTP_PASSWORD" ]; then
    echo "SMTP password is not set using env variable. Going to set one up"
    prompt_password
    export ACEOPERATOR_SMTP_PASSWORD=$password
fi

if [ -z "$ACEOPERATOR_RECAPTCHA_SECRET" ]; then
    echo "Recaptcha secret is not set using env variable. Going to set one up"
    prompt_password
    export ACEOPERATOR_RECAPTCHA_SECRET=$password
fi

$bin_dir/aceoperator-secret-from-env.sh | kubectl apply -f -

# create configmap for aceoperator
kubectl create configmap aceoperator "--from-env-file=$property_file"

# create a persistent volume claim
kubectl apply -f $template_dir/aceoperator-pvc.yml

# create aceoperatordb deployment
kubectl apply -f $template_dir/aceoperatordb-deployment.yml


# create aceoperatordb service
kubectl apply -f $template_dir/aceoperatordb-service.yml

# service to access mysql externally - DON'T DOIT in production environment
kubectl apply -f $template_dir/aceoperatordb-service-ext.yml 

# import certificate
kubectl create secret tls aceoperator-certs --save-config --key $certs_dir/kube.key --cert $certs_dir/kube.crt 

echo "Here is how the deployment looks so far:"
kubectl get all | sed '/^$/d'