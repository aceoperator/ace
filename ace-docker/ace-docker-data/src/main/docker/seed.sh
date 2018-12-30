#!/usr/bin/bash

src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
sql_dir="$target_dir/.ace/sql"

mkdir -p $target_dir

if [ ! -d "$target_dir/.ace" ]; then
    # copy the src to persistent storage volume
    echo "$target_dir/.ace does not exist. Initializing"
    mkdir -p $target_dir
    cp -R -p $src_dir/.ace $target_dir/
    $bin_dir/replace_in_files.sh '^ACEOPERATOR_.*' $(find $target_dir/.ace -type f -name '*sql')

    if [ "$ACE3_DATA_EMAIL_TRANSCRIPT" = "true" ]; then
        echo "Enabling email trascripts"
        sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF/; s/transcriptEmailFrom=@SELF/transcriptEmailFrom=@noreply@ace3.io/' \
            $target_dir/.ace/profiles/default-operator.properties
    fi
fi

db_exists=$(mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD -e 'SHOW DATABASES' | grep -i $ACEOPERATOR_SQL_DB)
if [ -z "$db_exists" ]; then
    echo "Database $ACEOPERATOR_SQL_DB does not exist. Creating database"
    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD < $target_dir/.ace/sql/init_db.sql

    objs_exists=$(mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD  $ACEOPERATOR_SQL_DB -e 'SHOW TABLES' | grep -i account_tbl)
    if [ -z "$data_exists" ]; then
        mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD < $target_dir/.ace/sql/init_objects.sql

        if [ -n "$ACE3_DATA_LOAD_USERS" ]; then
            IFS=$'\n'
            for e in $(echo $ACE3_DATA_USERS | tr ',' '\n'); do
                user=$(echo $e | cut -d ":" -f 1)
                password=$(echo $e | cut -d ":" -f 2)
                full_name=$(echo $e | cut -d ":" -f 3)
                email=$(echo $e | cut -d ":" -f 4)
                if [ -z "$user" ] || [ -z "$password" ] || [ -z "full_name" ] || [ -z "$email" ]; then
                    echo "One or more user attributes have not been set. Going to not create the user"
                else
                    echo "Creating user: $user, $full_name, $email"
                    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
                        -e "set @user='$user'; set @password='$password'; set @full_name='$full_name'; set @email='$email'; source $sql_dir/init_demo_user.sql;" 
                fi
            done
            unset IFS
        fi
    # TODO add patch logic here
    fi
fi
