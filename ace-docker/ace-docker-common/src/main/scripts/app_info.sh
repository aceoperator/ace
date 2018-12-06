#!/bin/sh

cnt_init() {
    echo "$1,$(date +'%s'),INIT" > /tmp/appstate; nc -k -l $2 -c "cat /tmp/appstate"&
    # TODO FIXME (the command below is returning the echo command pid
    return $!
}

cnt_change_state() {
    echo "$1,$(date +'%s'),$2" > /tmp/appstate
}

cnt_kill() {
    kill $1
}

cnt_waitfor() {
    while [ -n "true" ]; do
        message=$(nc --append-output --recv-only $1 $2)
        ret=$?
        if [ "$ret" = "0" ]; then
            # ping succeeded
            state=$(echo $message | awk -F ',' '{print $3}')
            # echo "Connection successful - $state"
            if [ "$state" = "$3" ]; then
                break
            fi
        fi
        sleep 5
    done
}
