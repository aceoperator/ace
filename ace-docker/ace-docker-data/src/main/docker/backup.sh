#!/bin/bash

max_backup=5
if [ -n "$1" ]; then
    max_backup=$1
fi

target_dir="/var/aceoperator"
mkdir -p $target_dir/backup
cd $target_dir/backup

db_backup_file="db_backup"

if [ -f "$db_backup_file.sql" ]; then
    mv $db_backup_file.sql ${db_backup_file}_$(stat -c%Z $db_backup_file.sql).sql
fi

mysqldump --no-create-db -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT \
    -u root -p$ACEOPERATOR_SQL_ROOT_PASSWORD $ACEOPERATOR_SQL_DB > $db_backup_file.sql

declare -i count
count=0
for file in $(ls -t *.sql); do
    if [ "$file" = "$db_backup_file.sql" ]; then
        # Today's backup
        continue
    fi

    count=$count+1
    if [ $count -ge $max_backup ]; then
        rm -f $file
    fi
done

# Now to .ace backup
ace_backup_file="ace_backup"

if [ -f "$ace_backup_file.tar.gz" ]; then
    mv $ace_backup_file.tar.gz ${ace_backup_file}_$(stat -c%Z $ace_backup_file.tar.gz).tar.gz
fi

tar cvzf $ace_backup_file.tar.gz --directory $target_dir .ace patches

count=0
for file in $(ls -t *.tar.gz); do
    if [ "$file" = "$ace_backup_file.tar.gz" ]; then
        # Today's backup
        continue
    fi

    count=$count+1
    if [ $count -ge $max_backup ]; then
        rm -f $file
    fi
done