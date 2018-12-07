#!/bin/sh

# The command line params are meant for manual execution from the command line. In docker envuironment,
# no params must be supplied and is totally driven by environment variables.

src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
sql_dir="$target_dir/.ace/sql"
ping_port=6969

. $bin_dir/cntping.sh

# signal to other containers that the data container is initializing
cnt_init $ping_port

# wait for mariadb to start
maria_started=1
while [ "$maria_started" = "1" ]; do
    sleep 5
    nc -zv $ACE_SQL_HOST 3306
    maria_started=$?
done

if [ -n "$RUN_SEED" ]; then
    if [ ! -d "$target_dir/.ace" ]; then
        echo "$target_dir/.ace does not exist. Initializing"
        mkdir -p $target_dir
        cp -R -p $src_dir/.ace $target_dir/
        $bin_dir/replace_in_files.sh '^ACE_.*' `find $target_dir/.ace -type f \( -name "*properties" -o -name "*sql" \)`
    fi

    db_exists=`mysql -h $ACE_SQL_HOST -u root -p$ACE_SQL_ROOT_PASSWORD -e 'SHOW DATABASES' | grep -i webtalk`
    if [ -z "$db_exists" ]; then
        echo "Database webtalk does not exist. Creating and loading data"
        mysql -h $ACE_SQL_HOST -u root -p$ACE_SQL_ROOT_PASSWORD < $target_dir/.ace/sql/init_db.sql
        mysql -h $ACE_SQL_HOST -u $ACE_SQL_USER -p$ACE_SQL_PASSWORD < $target_dir/.ace/sql/init_objects.sql

        if [ -n "$LOAD_DEMO" ]; then
            mysql -h $ACE_SQL_HOST -u $ACE_SQL_USER -p$ACE_SQL_PASSWORD < $sql_dir/init_demo_users.sql
            sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF;operations@acedemo.net/' \
                $target_dir/.ace/profiles/default-operator.properties 
        fi
    fi
fi

# signal to other containers that the data initilization is done
cnt_chstate $ping_port STARTED

# wait for self to end. This is going to sleep infinitely until the signal handler sets the state to ENDED (NYI)
cnt_waitfor 127.0.0.1 $ping_port ENDED