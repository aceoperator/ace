#!/bin/sh

src_patch_dir="/usr/share/aceoperator/.ace/patches"
target_patch_dir="/var/aceoperator/patches"
sql_dir="/var/aceoperator/.ace/sql"

# Create a list of database patches
mysql -s -N -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
    -e 'SELECT file_name FROM patchlist ORDER BY file_name' > /tmp/patches.txt

# Append the list of database patches
if [ -f "$target_patch_dir/patchlist.txt" ]; then
    cat $target_patch_dir/patchlist.txt | awk -F ',' '{print $1}' >> /tmp/patches.txt
fi

patchlist=
for entry in "$(sort /tmp/patches.txt)"; do
    patchlist="$entry,$patchlist"
done

echo "** List of applied patches **"
echo "$patchlist"
echo "** ************************ *"

if [ -d "$src_patch_dir" ] && [ -n "$(ls $src_patch_dir/patch_* 2> /dev/null)" ]; then
    save_cwd=$(pwd)
    cd $src_patch_dir
    for patch in $(ls -1 patch_*); do
        if [ -z "$(echo "$patchlist" | grep $patch)" ]; then
            # A patch has not been applied, apply it
            if [ -n "$(echo $patch | grep '.*\.sql')" ]; then
                # Apply a SQL patch
                echo "Applying SQL patch - $patch"
                mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
                    -e "source $patch"
                if [ "$?" -eq 0 ]; then
                    echo "Patch - $patch successfully applied"
                    mysql -h $ACEOPERATOR_SQL_HOST -P $ACEOPERATOR_SQL_PORT -u $ACEOPERATOR_SQL_USER -p$ACEOPERATOR_SQL_PASSWORD $ACEOPERATOR_SQL_DB \
                            -e "set @file='$patch'; source $sql_dir/apply_patch.sql;"
                else
                    echo "Failed to apply patch $patch . Will attempt on the next run with possibly bad consequences"
                fi 
            elif [ -n "$(echo $patch | grep '.*\.sh')" ]; then
                # Apply a bash patch
                echo "Applying Bash patch - $patch"
                chmod +x $patch
                ./$patch
                if [ "$?" -eq 0 ]; then
                    echo "Patch - $patch successfully applied"
                    echo "$patch,$(date '+%Y-%m-%d %H:%M:%S')" >> $target_patch_dir/patchlist.txt
                else
                    echo "Failed to apply patch $patch . Will attempt on the next run with possibly bad consequences"
                fi
            else
                echo "Not going to apply unknown type of patch $patch"
            fi
        fi
    done
    cd $save_cwd
fi


 