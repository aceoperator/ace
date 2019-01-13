#!/bin/bash

max_backup=5
if [ -n "$1" ]; then
    max_backup=$1
fi

backup_file="backup"
target_dir="/var/aceoperator"

mkdir -p $target_dir/backup
cd $target_dir/backup

if [ -f "$backup_file.sql" ]; then
    mv $backup_file.sql ${backup_file}_$(stat -c%Z $backup_file.sql).sql
fi

mysqldump --no-create-db -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT \
    -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD $ACEOPERATOR_SQL_DB > $backup_file.sql

declare -i count
count=0
for file in $(ls -t *.sql); do
    if [ "$file" = "$backup_file.sql" ]; then
        # Today's backup
        continue
    fi

    count=$count+1
    if [ $count -ge $max_backup ]; then
        rm -f $file
    fi
done


