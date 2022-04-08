FROM quay.io/keycloak/keycloak:17.0.1-legacy

RUN mkdir -p /opt/jboss/keycloak/providers
ADD target/keycloak-update-email-with-verification.jar /opt/jboss/keycloak/providers/keycloak-update-email-with-verification.jar

RUN mkdir -p /opt/jboss/keycloak/themes/base/login/messages
ADD themes/base/login/theme.properties                             /opt/jboss/keycloak/themes/base/login/theme.properties
ADD themes/base/login/update-email-with-verification.ftl           /opt/jboss/keycloak/themes/base/login/update-email-with-verification.ftl
ADD themes/base/login/update-email-with-verification-challenge.ftl /opt/jboss/keycloak/themes/base/login/update-email-with-verification-challenge.ftl
ADD themes/base/login/update-email-with-verification-complete.ftl  /opt/jboss/keycloak/themes/base/login/update-email-with-verification-complete.ftl
ADD themes/base/login/messages/messages_en.properties              /opt/jboss/keycloak/themes/base/login/messages/messages_en.properties
ADD themes/base/login/messages/messages_ja.properties              /opt/jboss/keycloak/themes/base/login/messages/messages_ja.properties

RUN mkdir -p /opt/jboss/keycloak/themes/base/email/text
RUN mkdir -p /opt/jboss/keycloak/themes/base/email/html
RUN mkdir -p /opt/jboss/keycloak/themes/base/email/messages
ADD themes/base/email/theme.properties                        /opt/jboss/keycloak/themes/base/email/theme.properties
ADD themes/base/email/text/update-email-with-verification.ftl /opt/jboss/keycloak/themes/base/email/text/update-email-with-verification.ftl
ADD themes/base/email/html/update-email-with-verification.ftl /opt/jboss/keycloak/themes/base/email/html/update-email-with-verification.ftl
ADD themes/base/email/messages/messages_en.properties         /opt/jboss/keycloak/themes/base/email/messages/messages_en.properties
ADD themes/base/email/messages/messages_ja.properties         /opt/jboss/keycloak/themes/base/email/messages/messages_ja.properties
