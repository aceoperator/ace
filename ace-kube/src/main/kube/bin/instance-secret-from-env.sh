#!/bin/sh

function convert {
  if [ -z "$1" ]; then echo "Warning: value is empty" >&2; echo -n \"\"; else echo -n "$1" | base64 ; fi
}

instance=webtalk
if [ -n "$1" ]; then
  instance=$1
fi

cat << EOF
apiVersion: v1
kind: Secret
metadata:
  name: $instance
type: Opaque
data:
   mysql_ace_password: $(convert "$ACEOPERATOR_SQL_PASSWORD")
   admin_password: $(convert "$ACEOPERATOR_ADMIN_PASSWORD")
EOF

