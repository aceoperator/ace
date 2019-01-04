#!/bin/sh

function convert {
  if [ -z "$1" ]; then echo "Warning: value empty" >&2; echo -n \"\"; else echo -n "$1" | base64 -w 0; fi
}

cat << EOF
apiVersion: v1
kind: Secret
metadata:
  name: aceoperator
type: Opaque
data:
  mysql_root_password: $(convert "$ACEOPERATOR_SQL_ROOT_PASSWORD")
  smtp_password: $(convert "$ACEOPERATOR_SMTP_PASSWORD")
  recaptcha_secret: $(convert "$ACEOPERATOR_RECAPTCHA_SECRET")
EOF