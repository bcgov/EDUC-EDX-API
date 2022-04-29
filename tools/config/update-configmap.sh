envValue=$1
APP_NAME=$2
OPENSHIFT_NAMESPACE=$3
COMMON_NAMESPACE=$4
DB_JDBC_CONNECT_STRING=$5
DB_PWD=$6
DB_USER=$7
SPLUNK_TOKEN=$8
APP_NAME_UPPER=${APP_NAME^^}


TZVALUE="America/Vancouver"
SOAM_KC_REALM_ID="master"

SOAM_KC_LOAD_USER_ADMIN=$(oc -n $COMMON_NAMESPACE-$envValue -o json get secret sso-admin-${envValue} | sed -n 's/.*"username": "\(.*\)"/\1/p' | base64 --decode)
SOAM_KC_LOAD_USER_PASS=$(oc -n $COMMON_NAMESPACE-$envValue -o json get secret sso-admin-${envValue} | sed -n 's/.*"password": "\(.*\)",/\1/p' | base64 --decode)

SOAM_KC=soam-$envValue.apps.silver.devops.gov.bc.ca
NATS_CLUSTER=educ_nats_cluster
NATS_URL="nats://nats.${COMMON_NAMESPACE}-${envValue}.svc.cluster.local:4222"

echo Fetching SOAM token
TKN=$(curl -s \
  -d "client_id=admin-cli" \
  -d "username=$SOAM_KC_LOAD_USER_ADMIN" \
  -d "password=$SOAM_KC_LOAD_USER_PASS" \
  -d "grant_type=password" \
  "https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID/protocol/openid-connect/token" | jq -r '.access_token')

###########################################################
#Setup for scopes
###########################################################

echo
echo Writing scope READ_SECURE_EXCHANGE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read scope for secure exchange\",\"id\": \"READ_SECURE_EXCHANGE\",\"name\": \"READ_SECURE_EXCHANGE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_SECURE_EXCHANGE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for secure exchange\",\"id\": \"WRITE_SECURE_EXCHANGE\",\"name\": \"WRITE_SECURE_EXCHANGE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_SECURE_EXCHANGE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Delete scope for secure exchange\",\"id\": \"DELETE_SECURE_EXCHANGE\",\"name\": \"DELETE_SECURE_EXCHANGE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_DOCUMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"READ_SECURE_EXCHANGE_DOCUMENT\",\"name\": \"READ_SECURE_EXCHANGE_DOCUMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_DOCUMENT_REQUIREMENTS
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"READ_SEC_EXCHANGE_DOC_REQUIREMENTS\",\"name\": \"READ_SECURE_EXCHANGE_DOCUMENT_REQUIREMENTS\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_SECURE_EXCHANGE_DOCUMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"WRITE_SECURE_EXCHANGE_DOCUMENT\",\"name\": \"WRITE_SECURE_EXCHANGE_DOCUMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_DOCUMENT_TYPES
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"READ_SECURE_EXCHANGE_DOCUMENT_TYPES\",\"name\": \"READ_SECURE_EXCHANGE_DOCUMENT_TYPES\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_CODES
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"READ_SECURE_EXCHANGE_CODES\",\"name\": \"READ_SECURE_EXCHANGE_CODES\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_SECURE_EXCHANGE_DOCUMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"SOAM send email scope\",\"id\": \"DELETE_SECURE_EXCHANGE_DOCUMENT\",\"name\": \"DELETE_SECURE_EXCHANGE_DOCUMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_MINISTRY_TEAMS
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Reading ministry teams in EDX\",\"id\": \"READ_MINISTRY_TEAMS\",\"name\": \"READ_MINISTRY_TEAMS\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_EDX_USER_SCHOOLS
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Reading user schools in EDX\",\"id\": \"READ_EDX_USER_SCHOOLS\",\"name\": \"READ_EDX_USER_SCHOOLS\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_EDX_USERS
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Reading users in EDX\",\"id\": \"READ_EDX_USERS\",\"name\": \"READ_EDX_USERS\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

###########################################################
#Setup for config-map
###########################################################
SPLUNK_URL="gww.splunk.educ.gov.bc.ca"
FLB_CONFIG="[SERVICE]
   Flush        1
   Daemon       Off
   Log_Level    debug
   HTTP_Server   On
   HTTP_Listen   0.0.0.0
   Parsers_File parsers.conf
[INPUT]
   Name   tail
   Path   /mnt/log/*
   Exclude_Path *.gz,*.zip
   Parser docker
   Mem_Buf_Limit 20MB
[FILTER]
   Name record_modifier
   Match *
   Record hostname \${HOSTNAME}
[OUTPUT]
   Name   stdout
   Match  *
[OUTPUT]
   Name  splunk
   Match *
   Host  $SPLUNK_URL
   Port  443
   TLS         On
   TLS.Verify  Off
   Message_Key $APP_NAME
   Splunk_Token $SPLUNK_TOKEN
"
PARSER_CONFIG="
[PARSER]
    Name        docker
    Format      json
"
echo
echo Creating config map "$APP_NAME"-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map --from-literal=TZ=$TZVALUE --from-literal=TOKEN_ISSUER_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID" --from-literal=NATS_URL="$NATS_URL" --from-literal=NATS_CLUSTER=$NATS_CLUSTER --from-literal=JDBC_URL="$DB_JDBC_CONNECT_STRING" --from-literal=DB_USERNAME="$DB_USER" --from-literal=DB_PASSWORD="$DB_PWD" --from-literal=SPRING_SECURITY_LOG_LEVEL=INFO --from-literal=SPRING_WEB_LOG_LEVEL=INFO --from-literal=APP_LOG_LEVEL=INFO --from-literal=HIBERNATE_STATISTICS=false --from-literal=SPRING_BOOT_AUTOCONFIG_LOG_LEVEL=INFO --from-literal=SPRING_SHOW_REQUEST_DETAILS=false --from-literal=FILE_EXTENSIONS="image/jpeg,image/png,application/pdf,.jpg,.jpeg,.jpe,.jfif,.jif,.jfi" --from-literal=FILE_MAXSIZE=10485760  --from-literal=FILE_MAX_ENCODED_SIZE=15485760  --from-literal=BCSC_AUTO_MATCH_OUTCOMES="RIGHTPEN,WRONGPEN,ZEROMATCHES,MANYMATCHES,ONEMATCH" --from-literal=REMOVE_BLOB_CONTENTS_DOCUMENT_AFTER_DAYS="365" --from-literal=SCHEDULED_JOBS_REMOVE_BLOB_CONTENTS_DOCUMENT_CRON="@midnight" --from-literal=NATS_MAX_RECONNECT=60 --dry-run -o yaml | oc apply -f -
echo

echo Setting environment variables for "$APP_NAME"-$SOAM_KC_REALM_ID application
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env --from=configmap/"$APP_NAME"-config-map dc/"$APP_NAME"-$SOAM_KC_REALM_ID

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map --from-literal=fluent-bit.conf="$FLB_CONFIG" --from-literal=parsers.conf="$PARSER_CONFIG" --dry-run=client -o yaml | oc apply -f -

echo Removing un-needed config entries
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env dc/"$APP_NAME"-$SOAM_KC_REALM_ID KEYCLOAK_PUBLIC_KEY-
