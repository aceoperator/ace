#!/bin/bash

function cleanup {
  # Your cleanup code here
  echo "Application is being shutdown"
  touch /var/aceoperator/stopped
}
trap finish SIGTERM

src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
ping_port=6970
data_ping_port=6969

. $bin_dir/cntping.sh

if [ "$ACE3_CNTSYNC" = "true" ]; then
    # signal to other containers that the data container is initializing
    cnt_init $ping_port

    # wait for the data container to start
    cnt_waitfor $ACE3_APP_DATA_HOST $data_ping_port STARTED
fi

export JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8 -Duser.timezone=US/Eastern -Dace.root.dir=$target_dir"

# TODO add option to add trust store

# add all the ACEOPERATOR environment variables as system properties with a "env." prefix
for e in $(env | grep '^ACEOPERATOR' | awk -F "=" '{ print " -Denv."$1"="$2}'); do export JAVA_OPTS="$JAVA_OPTS $e"; done

if [ "$ACE3_CNTSYNC" = "true" ]; then
    # signal to other containers that app initilization is done
    # TODO change this to run after tomcat has started
    cnt_chstate $ping_port STARTED
fi

# Start tomcat
catalina.sh run
