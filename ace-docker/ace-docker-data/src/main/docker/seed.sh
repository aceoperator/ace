#!/usr/bin/bash


src_dir="/usr/share/aceoperator"
target_dir="/var/aceoperator"
bin_dir="$src_dir/bin"
sql_dir="$target_dir/.ace/sql"

mkdir -p $target_dir/patches

if [ ! -d "$target_dir/.ace" ]; then
    # copy the src to persistent storage volume
    echo "$target_dir/.ace does not exist. Initializing"
    mkdir -p $target_dir
    cp -R -p $src_dir/.ace $target_dir/
    $bin_dir/replace_in_files.sh '^ACEOPERATOR_.*' $(find $target_dir/.ace -type f -name '*sql')

    if [ "$ACE3_DATA_EMAIL_TRANSCRIPT" = "true" ]; then
        echo "Enabling email trascripts"
        sed -i -e 's/emailTranscript=false/emailTranscript=true/; s/transcriptEmailTo=@SELF;@OTHERS/transcriptEmailTo=@SELF/; s/transcriptEmailFrom=@SELF/transcriptEmailFrom=noreply@ace3.io/' \
            $target_dir/.ace/profiles/default-operator.properties
    fi

    # Since we initialized from scratch, we are going to assume that all patches so far as been applied as a part of the above init. 
    # Mark all the patches as applied
    if [ -d "$src_dir/patches" ]; then
        save_cwd=$(pwd)
        cd $src_dir/patches
        ls -1 patch_*.sh 2> /dev/null | awk -v curdate="$(date --iso-8601)" '{print $0","curdate}' > $target_dir/patches/patchlist.txt
        cd $save_cwd
    fi
fi

db_exists=$(mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD -e 'SHOW DATABASES' | grep -i $ACEOPERATOR_SQL_DB)
if [ -z "$db_exists" ]; then
    echo "Database $ACEOPERATOR_SQL_DB does not exist. Creating database"
    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD < $target_dir/.ace/sql/init_db.sql
fi

objs_exists=$(mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD  $ACEOPERATOR_SQL_DB -e 'SHOW TABLES' | grep -i account_tbl)
if [ -z "$objs_exists" ]; then
    # load data from backup if available
    backup_dir="$target_dir/backup"
    if [ -f "$backup_dir/backup.sql" ]; then
        echo "Database tables for $ACEOPERATOR_SQL_DB does not exist. Creating objects from backup"
        mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB < $backup_dir/backup.sql
    else
        echo "Database tables for $ACEOPERATOR_SQL_DB does not exist. Creating objects from source"
        mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD < $target_dir/.ace/sql/init_objects.sql

        # Instead of IFS, use - while read -e var; do echo -e $var; done
        if [ -n "$ACE3_DATA_LOAD_USERS" ]; then
            echo "Creating operators..."
            IFS=$'\n'
            for e in $(echo $ACE3_DATA_USERS | tr ',' '\n'); do
                user=$(echo $e | cut -d ":" -f 1)
                password=$(echo $e | cut -d ":" -f 2)
                full_name=$(echo $e | cut -d ":" -f 3)
                email=$(echo $e | cut -d ":" -f 4)
                if [ -z "$user" ] || [ -z "$password" ] || [ -z "full_name" ] || [ -z "$email" ]; then
                    echo "One or more user attributes have not been set. Going to not create the user"
                else
                    echo "Creating operator: $user, $full_name, $email"
                    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
                        -e "set @user='$user'; set @password='$password'; set @full_name='$full_name'; set @email='$email'; source $sql_dir/init_demo_user.sql;" 
                fi
            done
            unset IFS
        fi

        # Tables did not exist. We just created it.
        # Since we initialized from scratch, we are going to assume that all patches so far as been applied as a part of the above init. 
        # Mark all the patches as applied
        save_cwd=$(pwd)
        cd $src_dir/patches
        for patch in "$(ls -1 patch_*.sql 2> /dev/null)"; do
             mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
                        -e "set @file='$patch'; source $sql_dir/apply_patch.sql;" 
        done
        cd $save_cwd
    fi
fi
