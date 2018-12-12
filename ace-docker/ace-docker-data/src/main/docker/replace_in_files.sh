#!/bin/sh

pattern=\'"$1"\'
command="env | grep $pattern"
env_list=`eval $command`

exp_buffer=
for env in $env_list; do
    sed_exp=`echo $env | awk -F '=' '{printf "s/\\\\$\\\\$ACE(%s)/%s/g", $1, $2}'`
    if [ -n "$exp_buffer" ]; then
        exp_buffer=$exp_buffer";"
    fi
    exp_buffer=$exp_buffer$sed_exp    
done

shift
# echo "Replace expression: $exp_buffer"
eval sed -i -e \'$exp_buffer\' "$*"
