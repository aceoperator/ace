#!/bin/sh

# The command line params are meant for manual execution from the command line. In docker envuironment,
# no params must be supplied and is totally driven by environment variables.

src_dir="$1"
if [ -z "$src_dir" ]; then
    src_dir="/usr/share/aceoperator"
fi

target_dir="$2"
if [ -z "$target_dir" ]; then
    target_dir="/var/aceoperator"
fi

bin_dir="$3"
if [ -z "$bin_dir" ]; then
    bin_dir="$src_dir/bin"
fi

seed_dir="$4"
if [ -z "$seed_dir" ]; then
    seed_dir="$target_dir/.ace/sql"
fi

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
            mysql -h $ACE_SQL_HOST -u $ACE_SQL_USER -p$ACE_SQL_PASSWORD < $seed_dir/init_demo_users.sql
            sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF;operations@acedemo.net/' \
                $target_dir/.ace/profiles/default-operator.properties 
        fi
    fi
fi

if [ -n "$RUN_TOMCAT" ]; then
    export JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true -Dfile.encoding=UTF8 -Duser.timezone=US/Eastern -Dace.root.dir=$target_dir"

    # Start tomcat
    catalina.sh run
fi