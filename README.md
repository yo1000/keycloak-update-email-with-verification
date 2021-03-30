# keycloak-update-email-with-verification
Keycloak Update Email with Verification

## How to build
```
./mvnw clean package
```

## How to Run for local dev
```
./mvnw clean package && docker-compose up --build
```

### Update Email demonstration

1. Move to http://localhost:8080/auth/realms/master/protocol/openid-connect/auth?client_id=account&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fauth%2Frealms%2Fmaster%2Fext%2Faccept-email-updates&response_type=code&kc_action=UPDATE_EMAIL_WITH_VERIFICATION
    1. Input `admin123@localhost` to [Email]
    2. Click [Submit] button
2. Move to http://localhost:8025/ and Click [Inbox] link
    1. Open email and Click `Verify email address` link in body of text that starts with
       `http://localhost:8080/auth/realms/master/login-actions/action-token?`

Note:
It is possible to change the combination of `client_id` and `redirect_uri` to redirect to the URI of your choice.
(The URI you want to redirect to must be allowed in [Valid Redirect URIs] in the [Clients] settings.)
