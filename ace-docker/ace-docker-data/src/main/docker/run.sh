#!/bin/sh

bin_dir="/usr/share/aceoperator/bin"
ping_port="6969"

. $bin_dir/cntping.sh

# signal to other containers that the data container is initializing
cnt_init $ping_port

# wait for mariadb to start
maria_started=1
while [ "$maria_started" -ne 0 ]; do
    sleep 5
    ncat -zv $ACEOPERATOR_SQL_HOST $ACEOPERATOR_SQL_PORT
    maria_started=$?
done
echo "Mariadb is running. Going to continue with data operations"

if [ -n "$RUN_SEED" ]; then
    $bin_dir/seed.sh
    echo "Done seeing"
fi

# signal to other containers that the data initilization is done
cnt_chstate $ping_port STARTED

# wait for self to end. This is going to sleep infinitely until the signal handler sets the state to ENDED (NYI)
cnt_waitfor 127.0.0.1 $ping_port ENDED