#!/bin/sh

src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
ping_port=6970
data_ping_port=6969

. $bin_dir/cntping.sh

# signal to other containers that the data container is initializing
cnt_init $ping_port

# wait for the data container to start
cnt_waitfor $ACE_DATA_HOST $data_ping_port STARTED

export JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8 -Duser.timezone=US/Eastern -Dace.root.dir=$target_dir"

# signal to other containers that the data initilization is done
cnt_chstate $ping_port STARTED

# Start tomcat
catalina.sh run
