#!/bin/sh

function convert {
  if [ -z "$1" ]; then echo -n \"\"; else echo -n "$1" | base64 ; fi
}

cat << EOF
apiVersion: v1
kind: Secret
metadata:
  name: webtalk
type: Opaque
data:
   mysql_ace_password: $(convert "$ACEOPERATOR_SQL_PASSWORD")
   admin_password: $(convert "$ACEOPERATOR_ADMIN_PASSWORD")
EOF

