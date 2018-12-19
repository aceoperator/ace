#!/bin/sh

exec=ncat

#
# $1 = portnum
# 
cnt_init() {
    echo "$1,$(date +'%s'),INIT" > /tmp/appstate_$1; $exec -k -l $1 -c "cat /tmp/appstate_$1"&
    export NC_PID_$1=$(ps ax | grep "$exec -k -l $1" | grep -v "grep" | awk '{print $1}')
    echo "Going to listen on port $1 for container pings"
}

#
# $1 = portnum
# $2 = state
cnt_chstate() {
    echo "$1,$(date +'%s'),$2" > /tmp/appstate_$1
}

#
# $1 = portnum
#
cnt_kill() {
    eval kill \$NC_PID_$1
    echo "Killed $NC_PID"
}

#
# $1 = hostname
# $2 = portnum
# $3 = state
# 
cnt_waitfor() {
    con_log=0
    last_state=
    while [ -n "true" ]; do
        message=$($exec --append-output --recv-only $1 $2)
        ret=$?
        if [ "$ret" = "0" ]; then
            # ping succeeded
            if [ $con_log = "0" ]; then
                echo "Connection successful"
                con_log=1
            fi
            
            state=$(echo $message | awk -F ',' '{print $3}')

            if [ ! "$last_state" = "$state" ]; then
                echo "$2 is active and is in state - $state"
                last_state=$state
            fi

            if [ "$state" = "$3" ]; then
                break
            fi
        else
            con_log=0
            last_state=
        fi
        sleep 5
    done
}
