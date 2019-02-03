#!/bin/bash

function cleanup {
  echo "Shutting down ace-app..."
  catalina.sh stop
  while [ -n "$(ps ax | grep tomcat | grep -v grep)" ]; do
    sleep 1
  done
  echo "ace-app shutdown complete"
  exit 143 # 128 + 15 -- SIGTERM
}

# Kill the "tail" and then, shutdown gracefully
trap 'kill ${!}; cleanup' SIGTERM

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

JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8 -Duser.timezone=US/Eastern -Dace.root.dir=$target_dir"

# hardcode the password since this is a public key store and does not have any confidential information
# and I could not find a way of creating a java keystore with no password
export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/etc/ssl/certs/truststore -Djavax.net.ssl.trustStorePassword=a1b2c3d4"

# add all the ACEOPERATOR environment variables as system properties with a "env." prefix
for e in $(env | grep '^ACEOPERATOR' | awk -F "=" '{ print " -Denv."$1"="$2}'); do export JAVA_OPTS="$JAVA_OPTS $e"; done

# Start tomcat in background. Else the signal cannot be trapped and shutdown won't be graceful
catalina.sh run&

if [ "$ACE3_CNTSYNC" = "true" ]; then
    # signal to other containers that app initilization is done
    # TODO change this to run after tomcat has really started
    cnt_chstate $ping_port STARTED
fi

# wait forever
while true
do
  tail -f /dev/null & wait ${!}
done

echo "Exiting ace-app entrypoint"