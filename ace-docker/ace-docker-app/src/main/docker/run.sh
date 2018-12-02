#!/bin/sh

target_dir="/var/aceoperator"

# wait for seed to finish
seed_running="0"
while [ "$seed_running" = "0" ]; do
    sleep 5
    ping -c 1 data
    seed_running="$?"
done

export JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8 -Duser.timezone=US/Eastern -Dace.root.dir=$target_dir"

# Start tomcat
catalina.sh run