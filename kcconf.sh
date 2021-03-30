#!/bin/bash

# ================================================================================
# Request Access Token
# ================================================================================
KC_URI_BASE="http://keycloak:8080/auth"
KC_REALM="master"

KC_CONN_RETRY=1
KC_CONN_RETRY_MAX=255
while [[ "$(curl -D - -s -o /dev/null "${KC_URI_BASE}/" | head -n1 | sed -e 's/[^a-zA-Z0-9\-\ ]*//g' | awk '{print $2}')" != "200" ]] ; do
  if [[ $KC_CONN_RETRY -gt $KC_CONN_RETRY_MAX ]] ; then
    exit 1
  fi

  echo "Wait to retry connection to Keycloak (sleep: ${KC_CONN_RETRY}s)"
  sleep $KC_CONN_RETRY

  KC_CONN_RETRY=$(expr $KC_CONN_RETRY + $KC_CONN_RETRY)
done

KC_ACCESS_TOKEN=$(curl -XPOST -s \
  -d "client_id=admin-cli" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin1234" \
  "${KC_URI_BASE}/realms/${KC_REALM}/protocol/openid-connect/token" \
| jq -r ".access_token")

echo "
================================================================================
Requested Access Token
================================================================================
POST ${KC_URI_BASE}/realms/${KC_REALM}/protocol/openid-connect/token
- client_id=admin-cli
- grant_type=password
- username=admin
- password=admin1234
"

# ================================================================================
# Configure SMTP
#
# [Realm Settings] > [Email] tab
#   1. Input `mailhog` to [Host]
#   2. Input `1025` to [Port]
#   3. Input `postmaster@localhost` to [From]
#   4. Click [Save] button
# ================================================================================
curl -s -XPUT \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}" \
  -d '{
    "smtpServer" : {
      "host" : "mailhog",
      "port" : "1025",
      "from" : "postmaster@localhost"
    }
  }'

echo "
================================================================================
Configured SMTP
================================================================================
PUT ${KC_URI_BASE}/admin/realms/${KC_REALM}
{
  smtpServer : {
    host : mailhog,
    port : 1025,
    from : postmaster@localhost
  }
}
"

# ================================================================================
# Configure Required Actions
#
# [Authentication] > [Required Actions] tab
#   1. Click [Register] button
#   2. Chose `Update Email with Verification` in dropdown
#   3. Click [Ok] button
# ================================================================================
curl -s -XPOST \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/authentication/register-required-action" \
  -d '{
    "name" : "Update Email with Verification",
    "providerId" : "UPDATE_EMAIL_WITH_VERIFICATION"
  }'

echo "
================================================================================
Configured Required Actions
================================================================================
POST ${KC_URI_BASE}/admin/realms/${KC_REALM}/authentication/register-required-action
{
  name : Update Email with Verification,
  providerId : UPDATE_EMAIL_WITH_VERIFICATION
}
"

# ================================================================================
# Configure Valid Redirect URIs to Client (account)
#
# [Clients] > `account` link in [Lookup] tab > [Valid Redirect URIs] in [Settings] tab
#   1. Input `/realms/master/ext/*` to empty row
#   2. Click [Save] button
# ================================================================================
KC_CLIENT_ID=$(curl -s -XGET \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/clients" \
| jq -r '.[] | select(.clientId == "account") | .id')

curl -s -XPUT \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/clients/${KC_CLIENT_ID}" \
  -d '{
    "redirectUris" : [
      "/realms/master/account/*",
      "/realms/master/ext/*"
    ]
  }'

echo "
================================================================================
Configured Valid Redirect URIs to Client (account)
================================================================================
GET ${KC_URI_BASE}/admin/realms/${KC_REALM}/clients
PUT ${KC_URI_BASE}/admin/realms/${KC_REALM}/authentication/register-required-action
{
  name : Update Email with Verification,
  providerId : UPDATE_EMAIL_WITH_VERIFICATION
}
"

# ================================================================================
# Configure Users
# ================================================================================
KC_USER_ID_ADMIN=$(curl -s -XGET \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/users" \
  -d "username=admin" \
| jq -r ".[0].id")

curl -s -XPUT \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/users/${KC_USER_ID_ADMIN}" \
  -d '{
    "email"         : "admin@localhost",
    "firstName"     : "Alice",
    "lastName"      : "Admin",
    "emailVerified" : true
  }'

curl -s -XPOST \
  -H "Content-Type: application/json;charset=UTF-8" \
  -H "Authorization: bearer ${KC_ACCESS_TOKEN}" \
  "${KC_URI_BASE}/admin/realms/${KC_REALM}/users" \
  -d '{
    "username"      : "user",
    "email"         : "user@localhost",
    "firstName"     : "Bob",
    "lastName"      : "User",
    "enabled"       : true,
    "emailVerified" : true,
    "credentials" : [{
      "type"        : "password",
      "temporary"   : false,
      "value"       : "user1234"
    }]
  }'

echo "
================================================================================
Configured Users
================================================================================
GET ${KC_URI_BASE}/admin/realms/${KC_REALM}/users
- username=admin
PUT ${KC_URI_BASE}/admin/realms/${KC_REALM}/users/${KC_USER_ID_ADMIN}
{
  email         : admin@localhost,
  firstName     : Alice,
  lastName      : Admin,
  emailVerified : true
}
POST ${KC_URI_BASE}/admin/realms/${KC_REALM}/users
{
  username      : user,
  email         : user@localhost,
  firstName     : Bob,
  lastName      : User,
  enabled       : true,
  emailVerified : true,
  credentials : [{
    type        : password,
    temporary   : false,
    value       : user1234
  }]
}
"
