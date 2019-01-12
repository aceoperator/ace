#!/bin/sh

# ------------------------------------------------------------
# Setup environment variables
# ------------------------------------------------------------

export ACEOPERATOR_HOME=$HOME/aceoperator

export ACEOPERATOR_SQL_HOST=db
export ACEOPERATOR_SQL_PORT=3306
export ACEOPERATOR_SQL_ROOT_PASSWORD=a1b2c3d4
export ACEOPERATOR_SQL_USER=ace
export ACEOPERATOR_SQL_PASSWORD=a1b2c3d4
export ACEOPERATOR_SQL_DB=webtalk

export ACEOPERATOR_ADMIN_PASSWORD=a1b2c3d4

export ACEOPERATOR_SMTP_USER=
export ACEOPERATOR_SMTP_PASSWORD=
export ACEOPERATOR_SMTP_SERVER=127.0.0.1
export ACEOPERATOR_SMTP_PORT=25
export ACEOPERATOR_MAIL_ENCRYPT=false
export ACEOPERATOR_MAIL_OVERRIDE_FROM=

export ACEOPERATOR_RECAPTCHA_SECRET=

export ACE3_DATA_LOAD_USERS=true
export ACE3_DATA_EMAIL_TRANSCRIPT=true
export ACE3_DATA_USERS=
export ACE3_DATA_RUN_SEED=true
export ACE3_DATA_RUN_PATCH=true

export ACE3_APP_DATA_HOST=data

export ACE3_FE_APP_HOST=app
export ACE3_FE_CERTS_DIR=$HOME/certs