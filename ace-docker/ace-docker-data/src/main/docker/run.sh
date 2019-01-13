#!/bin/bash

bin_dir="/usr/share/aceoperator/bin"
ping_port="6969"
app_ping_port="6970"

. $bin_dir/cntping.sh

function cleanup {
    echo "Shutting down ace-data ..."
    # TODO figure out what kind of cleanup is needed
    echo "ace-data shutdown complete"
    exit 143 # 128 + 15 -- SIGTERM
}

# Kill the "tail" and then, shutdown gracefully
trap 'kill ${!}; cleanup' SIGTERM

if [ "$ACE3_CNTSYNC" = "true" ]; then
    # signal to other containers that the data container is initializing
    cnt_init $ping_port
fi

# wait for mariadb to start
maria_started=1
while [ "$maria_started" -ne 0 ]; do
    sleep 5
    ncat -zv $ACEOPERATOR_SQL_HOST $ACEOPERATOR_SQL_PORT
    maria_started=$?
done
echo "Mariadb is running. Going to continue with data operations"

if [ -n "$ACE3_DATA_RUN_SEED" ]; then
    $bin_dir/seed.sh
    echo "Done seeding"
fi

if [ "$ACE3_CNTSYNC" = "true" ]; then
    # signal to the app container that the data initilization is done
    cnt_chstate $ping_port STARTED

    # wait forever
    while true
    do
    tail -f /dev/null & wait ${!}
    done
fi

echo "Exiting ace-data entrypoint"