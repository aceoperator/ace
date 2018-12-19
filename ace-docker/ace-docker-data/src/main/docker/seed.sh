#!/bin/sh

src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
sql_dir="$target_dir/.ace/sql"

if [ ! -d "$target_dir/.ace" ]; then
    echo "$target_dir/.ace does not exist. Initializing"
    mkdir -p $target_dir
    cp -R -p $src_dir/.ace $target_dir/
    $bin_dir/replace_in_files.sh '^ACEOPERATOR_.*' `find $target_dir/.ace -type f \( -name "*properties" -o -name "*sql" \)`
fi

db_exists=`mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD -e 'SHOW DATABASES' | grep -i webtalk`
if [ -z "$db_exists" ]; then
    echo "Database webtalk does not exist. Creating and loading data"
    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD < $target_dir/.ace/sql/init_db.sql
    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD < $target_dir/.ace/sql/init_objects.sql

    if [ -n "$LOAD_DEMO" ]; then
        mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD < $sql_dir/init_demo_users.sql
        sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF;operations@acedemo.net/' \
            $target_dir/.ace/profiles/default-operator.properties 
    fi
fi
