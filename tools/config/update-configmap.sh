envValue=$1
APP_NAME=$2
OPENSHIFT_NAMESPACE=$3
COMMON_NAMESPACE=$4
DB_JDBC_CONNECT_STRING=$5
DB_PWD=$6
DB_USER=$7
SPLUNK_TOKEN=$8
APP_NAME_UPPER=${APP_NAME^^}
CHES_CLIENT_ID=$9
CHES_CLIENT_SECRET="${10}"
CHES_TOKEN_URL="${11}"
CHES_ENDPOINT_URL="${12}"
SITE_URL="${13}"

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


echo
echo Writing scope WRITE_EDX_USER
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Writing users in EDX_USER\",\"id\": \"WRITE_EDX_USER\",\"name\": \"WRITE_EDX_USER\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_EDX_USER
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Deleting user from EDX_USER\",\"id\": \"DELETE_EDX_USER\",\"name\": \"DELETE_EDX_USER\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_EDX_USER_SCHOOL
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Deleting EDX_USER_SCHOOL \",\"id\": \"DELETE_EDX_USER_SCHOOL\",\"name\": \"DELETE_EDX_USER_SCHOOL\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_EDX_USER_SCHOOL
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Writing in EDX_USER_SCHOOL\",\"id\": \"WRITE_EDX_USER_SCHOOL\",\"name\": \"WRITE_EDX_USER_SCHOOL\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_EDX_USER_SCHOOL_ROLE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Writing  in EDX_USER_SCHOOL_ROLE\",\"id\": \"WRITE_EDX_USER_SCHOOL_ROLE\",\"name\": \"WRITE_EDX_USER_SCHOOL_ROLE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_EDX_USER_SCHOOL_ROLE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Deleting DELETE_EDX_USER_SCHOOL_ROLE \",\"id\": \"DELETE_EDX_USER_SCHOOL_ROLE\",\"name\": \"DELETE_EDX_USER_SCHOOL_ROLE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope ACTIVATE_EDX_USER
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Activate EDX_USER \",\"id\": \"ACTIVATE_EDX_USER\",\"name\": \"ACTIVATE_EDX_USER\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"


echo
echo Writing scope WRITE_ACTIVATION_CODE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write EDX_ACTIVATION_CODE \",\"id\": \"WRITE_ACTIVATION_CODE\",\"name\": \"WRITE_ACTIVATION_CODE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_ACTIVATION_CODE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Deleting EDX_ACTIVATION_CODE \",\"id\": \"DELETE_ACTIVATION_CODE\",\"name\": \"DELETE_ACTIVATION_CODE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_PRIMARY_ACTIVATION_CODE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read EDX_PRIMARY_ACTIVATION_CODE \",\"id\": \"READ_PRIMARY_ACTIVATION_CODE\",\"name\": \"READ_PRIMARY_ACTIVATION_CODE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_PRIMARY_ACTIVATION_CODE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write EDX_PRIMARY_ACTIVATION_CODE \",\"id\": \"WRITE_PRIMARY_ACTIVATION_CODE\",\"name\": \"WRITE_PRIMARY_ACTIVATION_CODE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

  echo
  echo Writing scope SCHOOL_USER_ACTIVATION_INVITE_SAGA
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"description\": \"Write SCHOOL_USER_ACTIVATION_INVITE_SAGA \",\"id\": \"SCHOOL_USER_ACTIVATION_INVITE_SAGA\",\"name\": \"SCHOOL_USER_ACTIVATION_INVITE_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

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
EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Edx School User Activation</title></head><body>Hi <span th:text=\"\${$}{firstName}\"></span>,<br><br><span th:text=\"\${$}{schoolName}\"></span> would like to invite you to use the Education Data Exchange (EDX).<br><br>To activate your access please complete the following steps:<ol><li>Access EDX via this link: <a th:href=\"@{\${\$}{activationLink}}\">here</a></li><li>Log into EDX using the credential of your choice</li><li>Enter the following information:<dd>- The mincode of <span th:text=\"\${$}{schoolName}\"></span></dd><dd>- The school's Primary Edx Code (this can be obtained from your school administrator)</dd><dd>- Your Personal Activation Code: <span th:text=\"\${$}{personalActivationCode}\"></span></dd></li></ol><br><br>If the above link doesn't work, please paste this link into your web browser's address field:<br><br><a th:href=\"@{\${\$}{activationLink}}\" th:text=\"\${\$}{activationLink}\"></a><br><br>Regards,<br>The Ministry of Education and Child Care's EDX Team</body></html>"

SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="0 0/1 * * * *"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="55s"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="57s"
echo
echo Creating config map "$APP_NAME"-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map --from-literal=TZ=$TZVALUE --from-literal=TOKEN_ISSUER_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID" --from-literal=NATS_URL="$NATS_URL" --from-literal=NATS_CLUSTER=$NATS_CLUSTER --from-literal=JDBC_URL="$DB_JDBC_CONNECT_STRING" --from-literal=DB_USERNAME="$DB_USER" --from-literal=DB_PASSWORD="$DB_PWD" --from-literal=SPRING_SECURITY_LOG_LEVEL=INFO --from-literal=SPRING_SHOW_SQL=false --from-literal=SPRING_WEB_LOG_LEVEL=INFO --from-literal=APP_LOG_LEVEL=INFO --from-literal=HIBERNATE_STATISTICS=false --from-literal=SPRING_BOOT_AUTOCONFIG_LOG_LEVEL=INFO --from-literal=SPRING_SHOW_REQUEST_DETAILS=false --from-literal=FILE_EXTENSIONS="image/jpeg,image/png,application/pdf,.jpg,.jpeg,.jpe,.jfif,.jif,.jfi" --from-literal=FILE_MAXSIZE=10485760  --from-literal=FILE_MAX_ENCODED_SIZE=15485760  --from-literal=BCSC_AUTO_MATCH_OUTCOMES="RIGHTPEN,WRONGPEN,ZEROMATCHES,MANYMATCHES,ONEMATCH" --from-literal=REMOVE_BLOB_CONTENTS_DOCUMENT_AFTER_DAYS="365" --from-literal=SCHEDULED_JOBS_REMOVE_BLOB_CONTENTS_DOCUMENT_CRON="@midnight" --from-literal=NATS_MAX_RECONNECT=60  --from-literal=CHES_CLIENT_ID="$CHES_CLIENT_ID" --from-literal=CHES_CLIENT_SECRET="$CHES_CLIENT_SECRET" --from-literal=CHES_TOKEN_URL="$CHES_TOKEN_URL" --from-literal=CHES_ENDPOINT_URL="$CHES_ENDPOINT_URL" --from-literal=NOTIFICATION_EMAIL_SWITCH_ON="true" --from-literal=EMAIL_SUBJECT_EDX_SCHOOL_USER_ACTIVATION_INVITE="Activate Your Account for the Education Data Exchange" --from-literal=EMAIL_FROM_EDX_SCHOOL_USER_ACTIVATION_INVITE="edx-noreply@gov.bc.ca" --from-literal=SITE_URL="$SITE_URL" --from-literal=EDX_SCHOOL_USER_ACTIVATION_INVITE_VALIDITY_HOURS="24" --from-literal=EDX_SCHOOL_USER_ACTIVATION_INVITE_URL_APPEND="/api/edx/activate-user-verification?validationCode=" --from-literal=EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE="$EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE_LITERAL" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR" --dry-run -o yaml | oc apply -f -
echo

echo Setting environment variables for "$APP_NAME"-$SOAM_KC_REALM_ID application
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env --from=configmap/"$APP_NAME"-config-map dc/"$APP_NAME"-$SOAM_KC_REALM_ID

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map --from-literal=fluent-bit.conf="$FLB_CONFIG" --from-literal=parsers.conf="$PARSER_CONFIG" --dry-run=client -o yaml | oc apply -f -

echo Removing un-needed config entries
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env dc/"$APP_NAME"-$SOAM_KC_REALM_ID KEYCLOAK_PUBLIC_KEY-
