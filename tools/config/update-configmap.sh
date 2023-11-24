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
#Setup for client
###########################################################

echo
echo Retrieving client ID for edx-api-service
PME_CLIENT_ID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  | jq '.[] | select(.clientId=="edx-api-service")' | jq -r '.id')

echo
echo Removing edx-api-service if exists
curl -sX DELETE "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$PME_CLIENT_ID" \
  -H "Authorization: Bearer $TKN"

echo
echo Creating client edx-api-service
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"clientId\" : \"edx-api-service\",\"surrogateAuthRequired\" : false,\"enabled\" : true,\"clientAuthenticatorType\" : \"client-secret\",\"redirectUris\" : [ ],\"webOrigins\" : [ ],\"notBefore\" : 0,\"bearerOnly\" : false,\"consentRequired\" : false,\"standardFlowEnabled\" : false,\"implicitFlowEnabled\" : false,\"directAccessGrantsEnabled\" : false,\"serviceAccountsEnabled\" : true,\"publicClient\" : false,\"frontchannelLogout\" : false,\"protocol\" : \"openid-connect\",\"attributes\" : {\"saml.assertion.signature\" : \"false\",\"saml.multivalued.roles\" : \"false\",\"saml.force.post.binding\" : \"false\",\"saml.encrypt\" : \"false\",\"saml.server.signature\" : \"false\",\"saml.server.signature.keyinfo.ext\" : \"false\",\"exclude.session.state.from.auth.response\" : \"false\",\"saml_force_name_id_format\" : \"false\",\"saml.client.signature\" : \"false\",\"tls.client.certificate.bound.access.tokens\" : \"false\",\"saml.authnstatement\" : \"false\",\"display.on.consent.screen\" : \"false\",\"saml.onetimeuse.condition\" : \"false\"},\"authenticationFlowBindingOverrides\" : { },\"fullScopeAllowed\" : true,\"nodeReRegistrationTimeout\" : -1,\"protocolMappers\" : [ {\"name\" : \"Client ID\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientId\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientId\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client Host\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientHost\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientHost\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client IP Address\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientAddress\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientAddress\",\"jsonType.label\" : \"String\"}} ],\"defaultClientScopes\" : [ \"web-origins\",\"WRITE_PEN_REQUEST_BATCH\",\"READ_PEN_REQUEST_BATCH\", \"READ_PEN_MATCH\", \"READ_STUDENT\",\"SOAM_LINK\", \"READ_SCHOOL\", \"READ_DISTRICT\", \"role_list\", \"profile\", \"roles\", \"email\"],\"optionalClientScopes\" : [ \"address\", \"phone\", \"offline_access\" ],\"access\" : {\"view\" : true,\"configure\" : true,\"manage\" : true}}"

echo
echo Retrieving client ID for edx-api-service
PME_APIServiceClientID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  | jq '.[] | select(.clientId=="edx-api-service")' | jq -r '.id')

echo
echo Retrieving client secret for edx-api-service
PME_APIServiceClientSecret=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$PME_APIServiceClientID/client-secret" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  | jq -r '.value')

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

echo
echo Writing scope CREATE_SECURE_EXCHANGE_COMMENT_SAGA
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"description\": \"Write CREATE_SECURE_EXCHANGE_COMMENT_SAGA \",\"id\": \"CREATE_SECURE_EXCHANGE_COMMENT_SAGA\",\"name\": \"CREATE_SECURE_EXCHANGE_COMMENT_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DISTRICT_USER_ACTIVATION_INVITE_SAGA
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"description\": \"Write DISTRICT_USER_ACTIVATION_INVITE_SAGA \",\"id\": \"DISTRICT_USER_ACTIVATION_INVITE_SAGA\",\"name\": \"DISTRICT_USER_ACTIVATION_INVITE_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope MOVE_SCHOOL_SAGA
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"description\": \"Write MOVE_SCHOOL_SAGA \",\"id\": \"MOVE_SCHOOL_SAGA\",\"name\": \"MOVE_SCHOOL_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope CREATE_SCHOOL_SAGA
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"description\": \"Write CREATE_SCHOOL_SAGA \",\"id\": \"CREATE_SCHOOL_SAGA\",\"name\": \"CREATE_SCHOOL_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope CREATE_SECURE_EXCHANGE_SAGA
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write CREATE_SECURE_EXCHANGE_SAGA \",\"id\": \"CREATE_SECURE_EXCHANGE_SAGA\",\"name\": \"CREATE_SECURE_EXCHANGE_SAGA\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_COMMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read scope for secure exchange comment\",\"id\": \"READ_SECURE_EXCHANGE_COMMENT\",\"name\": \"READ_SECURE_EXCHANGE_COMMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_SECURE_EXCHANGE_COMMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for secure exchange comment\",\"id\": \"WRITE_SECURE_EXCHANGE_COMMENT\",\"name\": \"WRITE_SECURE_EXCHANGE_COMMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_SECURE_EXCHANGE_COMMENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Delete scope for secure exchange comment\",\"id\": \"DELETE_SECURE_EXCHANGE_COMMENT\",\"name\": \"DELETE_SECURE_EXCHANGE_COMMENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_NOTE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read scope for secure exchange note\",\"id\": \"READ_SECURE_EXCHANGE_NOTE\",\"name\": \"READ_SECURE_EXCHANGE_NOTE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_SECURE_EXCHANGE_NOTE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for secure exchange note\",\"id\": \"WRITE_SECURE_EXCHANGE_NOTE\",\"name\": \"WRITE_SECURE_EXCHANGE_NOTE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_SECURE_EXCHANGE_NOTE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Delete scope for secure exchange note\",\"id\": \"DELETE_SECURE_EXCHANGE_NOTE\",\"name\": \"DELETE_SECURE_EXCHANGE_NOTE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_SECURE_EXCHANGE_STUDENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read scope for secure exchange student\",\"id\": \"READ_SECURE_EXCHANGE_STUDENT\",\"name\": \"READ_SECURE_EXCHANGE_STUDENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_SECURE_EXCHANGE_STUDENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for secure exchange student\",\"id\": \"WRITE_SECURE_EXCHANGE_STUDENT\",\"name\": \"WRITE_SECURE_EXCHANGE_STUDENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_SECURE_EXCHANGE_STUDENT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Delete scope for secure exchange student\",\"id\": \"DELETE_SECURE_EXCHANGE_STUDENT\",\"name\": \"DELETE_SECURE_EXCHANGE_STUDENT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_EDX_USER_DISTRICT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for EDX_USER_DISTRICT\",\"id\": \"WRITE_EDX_USER_DISTRICT\",\"name\": \"WRITE_EDX_USER_DISTRICT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_EDX_USER_DISTRICT
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Delete scope for EDX_USER_DISTRICT\",\"id\": \"DELETE_EDX_USER_DISTRICT\",\"name\": \"DELETE_EDX_USER_DISTRICT\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_EDX_USER_DISTRICT_ROLE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for EDX_USER_DISTRICT_ROLE\",\"id\": \"WRITE_EDX_USER_DISTRICT_ROLE\",\"name\": \"WRITE_EDX_USER_DISTRICT_ROLE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope DELETE_EDX_USER_DISTRICT_ROLE
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write scope for DELETE_EDX_USER_DISTRICT_ROLE\",\"id\": \"DELETE_EDX_USER_DISTRICT_ROLE\",\"name\": \"DELETE_EDX_USER_DISTRICT_ROLE\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

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
EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Edx School User Activation</title></head><body>Hi <span th:text=\"\${$}{recipient}\"></span>,<br><br><span th:text=\"\${$}{schoolName}\"></span> would like to invite you to use the Education Data Exchange (EDX).<br><br>To activate your access you will need:<ol><li>A Basic BCeID account - you can create one here if needed: <a href=\"https://www.bceid.ca/\">https://www.bceid.ca/</a></li><li>The school's Primary Activation Code - <span th:unless=\"\${$}{#strings.isEmpty(edxAdmins)}\">available from the EDX administrator(s) for your school: <span th:text=\"\${$}{edxAdmins}\"></span></span><span th:if=\"\${$}{#strings.isEmpty(edxAdmins)}\">an administrator will reach out to you with this information</span></li><li>Your Personal Activation Code - provided below</li></ol>When you have a Basic BCeID account and the Primary Activation Code you are ready to get started!<br><br><b>Steps to Activate your Access</b><ol><li>Access <a th:href=\"@{\${\$}{activationLink}}\">EDX</a></li><li>Enter your Basic BCeID username and password</li><li>Enter the school's Primary Activation Code</li><li>Enter your Personal Activation Code: <span th:text=\"\${$}{personalActivationCode}\"></span></li></ol><br>If the above link doesn't work, please paste this link into your web browser's address field:<br><br><a th:href=\"@{\${\$}{activationLink}}\" th:text=\"\${\$}{activationLink}\"></a><br><br>Regards,<br>The Ministry of Education and Child Care's EDX Team</body></html>"

EMAIL_TEMPLATE_EDX_DISTRICT_USER_ACTIVATION_INVITE_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Edx District User Activation</title></head><body>Hi <span th:text=\"\${$}{recipient}\"></span>,<br><br><span th:text=\"\${$}{districtName}\"></span> would like to invite you to use the Education Data Exchange (EDX).<br><br>To activate your access you will need:<ol><li>A Basic BCeID account - you can create one here if needed: <a href=\"https://www.bceid.ca/\">https://www.bceid.ca/</a></li><li>The districts's Primary Activation Code - <span th:unless=\"\${$}{#strings.isEmpty(edxAdmins)}\">available from the EDX administrator(s) for your district: <span th:text=\"\${$}{edxAdmins}\"></span></span><span th:if=\"\${$}{#strings.isEmpty(edxAdmins)}\">an administrator will reach out to you with this information</span></li><li>Your Personal Activation Code - provided below</li></ol>When you have a Basic BCeID account and the Primary Activation Code you are ready to get started!<br><br><b>Steps to Activate your Access</b><ol><li>Access <a th:href=\"@{\${\$}{activationLink}}\">EDX</a></li><li>Enter your Basic BCeID username and password</li><li>Enter the district's Primary Activation Code</li><li>Enter your Personal Activation Code: <span th:text=\"\${$}{personalActivationCode}\"></span></li></ol><br>If the above link doesn't work, please paste this link into your web browser's address field:<br><br><a th:href=\"@{\${\$}{activationLink}}\" th:text=\"\${\$}{activationLink}\"></a><br><br>Regards,<br>The Ministry of Education and Child Care's EDX Team</body></html>"

EMAIL_TEMPLATE_EDX_NEW_SECURE_EXCHANGE_NOTIFICATION_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>New Secure Exchange Message</title></head><body>Hi <span th:text=\"\${\$}{recipient}\"></span>,<br><br><span th:text=\"\${$}{instituteName}\"></span> has received a new secure message from the <span th:text=\"\${$}{ministryTeamName}\"></span>.<br><br>To view the message, please log into the Education Data Exchange: <a th:href=\"@{\${\$}{linkToEDX}}\">here</a><br><br>If the above link doesn't work, please paste this link into your web browser's address field:<br><br><a th:href=\"@{\${\$}{linkToEDX}}\" th:text=\"\${\$}{linkToEDX}\"></a><br><br>Regards,<br>The Ministry of Education and Child Care's EDX Team</body></html>"

EMAIL_TEMPLATE_EDX_SECURE_EXCHANGE_COMMENT_NOTIFICATION_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Message Added to Secure Exchange</title></head><body>Hi <span th:text=\"\${\$}{recipient}\"></span>,<br><br>The <span th:text=\"\${$}{ministryTeamName}\"></span> has added a new message to secure message #<span th:text=\"\${$}{messageSequenceNumber}\"></span> for <span th:text=\"\${$}{instituteName}\"></span>.<br><br>To view the message, please log into the Education Data Exchange: <a th:href=\"@{\${\$}{linkToEDX}}\">here</a><br><br>If the above link doesn't work, please paste this link into your web browser's address field:<br><br><a th:href=\"@{\${\$}{linkToEDX}}\" th:text=\"\${\$}{linkToEDX}\"></a><br><br>Regards,<br>The Ministry of Education and Child Care's EDX Team</body></html>"

EMAIL_TEMPLATE_EDX_SCHOOL_PRIMARY_CODE_NOTIFICATION_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Primary Activation Code</title></head><body><p>Hi <span th:text=\"\${\$}{recipient}\"></span>,</p><p>Here is the Primary Access Code for the Education Data Exchange (EDX) for <span th:text=\"\${\$}{minCode}\"></span> - <span th:text=\"\${\$}{instituteName}\"></span>: <span th:text=\"\${\$}{primaryCode}\"></span></p><p>Please keep this code safe. It will be required for the activation of each EDX account at your school. Once you have activated your EDX Administrator Account, you will be able to view the Primary Access Code through the School User Management screen - available under the \"Administration\" menu option.</p><p>Regards,<br/>The Ministry of Education and Child Care's EDX Team</p></body></html>"

EMAIL_TEMPLATE_EDX_DISTRICT_PRIMARY_CODE_NOTIFICATION_LITERAL="<!DOCTYPE html><html xmlns:th=\"http://www.thymeleaf.org\"><head><meta charset=\"ISO-8859-1\"><title>Primary Activation Code</title></head><body><p>Hi <span th:text=\"\${\$}{recipient}\"></span>,</p><p>Here is the Primary Access Code for the Education Data Exchange (EDX) for <span th:text=\"\${\$}{minCode}\"></span> - <span th:text=\"\${\$}{instituteName}\"></span>: <span th:text=\"\${\$}{primaryCode}\"></span></p><p>Please keep this code safe. It will be required for the activation of each EDX account at your district. Once you have activated your EDX Administrator Account, you will be able to view the Primary Access Code through the District User Management screen - available under the \"Administration\" menu option.</p><p>Regards,<br/>The Ministry of Education and Child Care's EDX Team</p></body></html>"

ROLES_ALLOW_LIST="EDX_DISTRICT_ADMIN,EDX_SCHOOL_ADMIN,STUDENT_DATA_COLLECTION,SECURE_EXCHANGE_SCHOOL,SECURE_EXCHANGE_DISTRICT,EDX_EDIT_SCHOOL,EDX_EDIT_DISTRICT"

if [ "$envValue" = "prod" ]
then
  ROLES_ALLOW_LIST="EDX_DISTRICT_ADMIN,EDX_SCHOOL_ADMIN,SECURE_EXCHANGE_SCHOOL,SECURE_EXCHANGE_DISTRICT,EDX_EDIT_SCHOOL,EDX_EDIT_DISTRICT"
fi

SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="0 0/1 * * * *"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="55s"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="57s"
SCHEDULED_JOBS_PURGE_EDX_USERS_CRON_LITERAL="0 30 0 * * *"
EDX_ACTIVATION_CODE_LENGTH="8"
EDX_ACTIVATION_CODE_VALID_CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
echo
echo Creating config map "$APP_NAME"-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map \
  --from-literal=STUDENT_API_ENDPOINT="http://student-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/student/" \
  --from-literal=EDX_API_CLIENT_ID="edx-api-service" \
  --from-literal=EDX_API_CLIENT_SECRET="$PME_APIServiceClientSecret" \
  --from-literal=TZ=$TZVALUE \
  --from-literal=TOKEN_ISSUER_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID" \
  --from-literal=NATS_URL="$NATS_URL" \
  --from-literal=NATS_CLUSTER=$NATS_CLUSTER \
  --from-literal=JDBC_URL="$DB_JDBC_CONNECT_STRING" \
  --from-literal=DB_USERNAME="$DB_USER" \
  --from-literal=DB_PASSWORD="$DB_PWD" \
  --from-literal=SPRING_SECURITY_LOG_LEVEL=INFO \
  --from-literal=SPRING_SHOW_SQL=false \
  --from-literal=SPRING_WEB_LOG_LEVEL=INFO \
  --from-literal=APP_LOG_LEVEL=INFO \
  --from-literal=HIBERNATE_STATISTICS=false \
  --from-literal=SPRING_BOOT_AUTOCONFIG_LOG_LEVEL=INFO \
  --from-literal=SPRING_SHOW_REQUEST_DETAILS=false \
  --from-literal=FILE_EXTENSIONS="image/jpeg,image/png,application/pdf,text/csv,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,.jpg,.jpeg,.jpe,.jfif,.jif,.jfi,.std,.ver,.csv,.doc,.docx,.xls,.xlsx" \
  --from-literal=FILE_MAXSIZE=10485760  \
  --from-literal=FILE_MAX_ENCODED_SIZE=15485760  \
  --from-literal=BCSC_AUTO_MATCH_OUTCOMES="RIGHTPEN,WRONGPEN,ZEROMATCHES,MANYMATCHES,ONEMATCH" \
  --from-literal=ROLES_ALLOW_LIST="$ROLES_ALLOW_LIST" \
  --from-literal=REMOVE_BLOB_CONTENTS_DOCUMENT_AFTER_DAYS="365" \
  --from-literal=SCHEDULED_JOBS_REMOVE_BLOB_CONTENTS_DOCUMENT_CRON="@midnight" \
  --from-literal=NATS_MAX_RECONNECT=60  \
  --from-literal=CHES_CLIENT_ID="$CHES_CLIENT_ID" \
  --from-literal=CHES_CLIENT_SECRET="$CHES_CLIENT_SECRET" \
  --from-literal=CHES_TOKEN_URL="$CHES_TOKEN_URL" \
  --from-literal=CHES_ENDPOINT_URL="$CHES_ENDPOINT_URL" \
  --from-literal=NOTIFICATION_EMAIL_SWITCH_ON="true" \
  --from-literal=EMAIL_SUBJECT_EDX_SCHOOL_USER_ACTIVATION_INVITE="Activate Your Account for the Education Data Exchange" \
  --from-literal=EMAIL_FROM_EDX_SCHOOL_USER_ACTIVATION_INVITE="edx-noreply@gov.bc.ca" \
  --from-literal=EMAIL_TEMPLATE_EDX_DISTRICT_USER_ACTIVATION_INVITE="$EMAIL_TEMPLATE_EDX_DISTRICT_USER_ACTIVATION_INVITE_LITERAL" \
  --from-literal=SITE_URL="$SITE_URL" \
  --from-literal=EDX_SCHOOL_USER_ACTIVATION_INVITE_VALIDITY_HOURS="72" \
  --from-literal=EDX_SCHOOL_USER_ACTIVATION_INVITE_URL_APPEND="/api/edx/activate-user-verification?validationCode=" \
  --from-literal=EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE="$EMAIL_TEMPLATE_EDX_SCHOOL_USER_ACTIVATION_INVITE_LITERAL" \
  --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON" \
  --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR" \
  --from-literal=SCHEDULED_JOBS_PURGE_EDX_USERS_CRON="$SCHEDULED_JOBS_PURGE_EDX_USERS_CRON_LITERAL" \
  --from-literal=EMAIL_SUBJECT_EDX_NEW_SECURE_EXCHANGE_NOTIFICATION="New Secure Exchange Message" \
  --from-literal=EMAIL_TEMPLATE_EDX_NEW_SECURE_EXCHANGE_NOTIFICATION="$EMAIL_TEMPLATE_EDX_NEW_SECURE_EXCHANGE_NOTIFICATION_LITERAL" \
  --from-literal=EMAIL_SUBJECT_EDX_SECURE_EXCHANGE_COMMENT_NOTIFICATION="Message Added to Secure Exchange" \
  --from-literal=EMAIL_TEMPLATE_EDX_SECURE_EXCHANGE_COMMENT_NOTIFICATION="$EMAIL_TEMPLATE_EDX_SECURE_EXCHANGE_COMMENT_NOTIFICATION_LITERAL" \
  --from-literal=EMAIL_SUBJECT_EDX_SCHOOL_PRIMARY_CODE_NOTIFICATION="Primary Access Code for Education Data Exchange" \
  --from-literal=EMAIL_TEMPLATE_EDX_SCHOOL_PRIMARY_CODE_NOTIFICATION="$EMAIL_TEMPLATE_EDX_SCHOOL_PRIMARY_CODE_NOTIFICATION_LITERAL" \
  --from-literal=EMAIL_TEMPLATE_EDX_DISTRICT_PRIMARY_CODE_NOTIFICATION="$EMAIL_TEMPLATE_EDX_DISTRICT_PRIMARY_CODE_NOTIFICATION_LITERAL" \
  --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR" \
  --from-literal=SCHEDULED_JOBS_PURGE_CLOSED_MESSAGES_CRON="@midnight" \
  --from-literal=PURGE_CLOSED_SECURE_EXCHANGE_AFTER_DAYS="365" \
  --from-literal=EDX_ACTIVATION_CODE_LENGTH="$EDX_ACTIVATION_CODE_LENGTH" \
  --from-literal=EDX_ACTIVATION_CODE_VALID_CHARACTERS="$EDX_ACTIVATION_CODE_VALID_CHARACTERS" \
  --from-literal=INSTITUTE_API_ENDPOINT="http://institute-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/institute" --dry-run -o yaml | oc apply -f -
echo

echo Setting environment variables for "$APP_NAME"-$SOAM_KC_REALM_ID application
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env --from=configmap/"$APP_NAME"-config-map dc/"$APP_NAME"-$SOAM_KC_REALM_ID

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map --from-literal=fluent-bit.conf="$FLB_CONFIG" --from-literal=parsers.conf="$PARSER_CONFIG" --dry-run=client -o yaml | oc apply -f -

echo Removing un-needed config entries
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env dc/"$APP_NAME"-$SOAM_KC_REALM_ID KEYCLOAK_PUBLIC_KEY-
