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
    echo "Usage: $0 <KUBE_HOME_DIR> [INSTANCE_NAME]"
    exit 1
fi

kube_home="$1"

bin_dir="$kube_home/bin"
template_dir="$kube_home/template"
properties_dir="$kube_home/property"
instance=webtalk

if [ -n "$3" ]; then
 instance="$3"
fi

property_file="$properties_dir/$instance.properties"
if [ ! -f  "$property_file" ]; then
    echo "The properties file for $instance does not exist. Going to use the default file"
    property_file="$properties_dir/instance.properties"
fi

 echo "Going to deploy instance - $instance"

# create secret for instance
if [ -z "$ACEOPERATOR_SQL_PASSWORD" ]; then
    echo "MYSQL password for $instance is not set using env variable. Going to set one up"
    prompt_password
    export ACEOPERATOR_SQL_PASSWORD=$password
fi

if [ -z "$ACEOPERATOR_ADMIN_PASSWORD" ]; then
    echo "Admin password for $instance is not set using env variable. Going to set one up"
    prompt_password
    export ACEOPERATOR_ADMIN_PASSWORD=$password
fi

$bin_dir/instance-secret-from-env.sh $instance | kubectl apply -f -

# create configmap for instance
kubectl create configmap $instance "--from-env-file=$property_file"

# create deployment for instance
sed "s/webtalk/$instance/g" $template_dir/instance-deployment.yml | kubectl apply -f -

# create a service for instance
sed "s/webtalk/$instance/g" $template_dir/instance-service.yml | kubectl apply -f -

# create an ingress for instance
sed "s/webtalk/$instance/g" $template_dir/instance-ingress.yml | kubectl apply -f -

nslookup $instance.aceoperator.net
if [ "$?" -ne 0 ]; then
    echo "Warning! DNS lookup for host $instance.aceoperator.net failed. You need to setup the DNS"
fi

echo "Here is how the deployment looks so far:"
kubectl get all | grep "$instance" | sed '/^$/d'