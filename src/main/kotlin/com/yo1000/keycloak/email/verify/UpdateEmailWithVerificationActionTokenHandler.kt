package com.yo1000.keycloak.email.verify

import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler
import org.keycloak.authentication.actiontoken.ActionTokenContext
import org.keycloak.events.Errors
import org.keycloak.events.EventType
import org.keycloak.exceptions.TokenVerificationException
import org.keycloak.forms.login.LoginFormsProvider
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.services.messages.Messages
import org.keycloak.sessions.AuthenticationSessionModel
import javax.ws.rs.core.Cookie
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

class UpdateEmailWithVerificationActionTokenHandler : AbstractActionTokenHandler<UpdateEmailWithVerificationActionToken>(
        UpdateEmailWithVerificationActionToken.TOKEN_TYPE,
        UpdateEmailWithVerificationActionToken::class.java,
        Messages.STALE_VERIFY_EMAIL_LINK,
        EventType.EXECUTE_ACTIONS,
        Errors.INVALID_TOKEN
) {
    override fun handleToken(token: UpdateEmailWithVerificationActionToken, tokenContext: ActionTokenContext<UpdateEmailWithVerificationActionToken>): Response {
        val keycloakSession: KeycloakSession = tokenContext.session
        val authnSession: AuthenticationSessionModel = tokenContext.authenticationSession
        val realm: RealmModel = tokenContext.realm
        val user = authnSession.authenticatedUser
        val uriInfo: UriInfo = tokenContext.uriInfo
        val conn = tokenContext.clientConnection
        val event = tokenContext.event
        val request = tokenContext.request

        val newEmail: String = token.newEmail
                ?: throw TokenVerificationException(token, "New email linked to token does not exist")
        val oldEmail: String = token.oldEmail
                ?: throw TokenVerificationException(token, "Old email linked to token does not exist")

        // Update Email
        user.email = newEmail

        keycloakSession.userCache().evict(realm, user)

        event.clone().event(EventType.UPDATE_EMAIL)
                .detail("previous_email", oldEmail)
                .detail("updated_email", newEmail).success()

        event.success()

        return keycloakSession.getProvider(LoginFormsProvider::class.java)
                .setAuthenticationSession(authnSession)
                .setMessageAttribute("updateEmailWithVerificationCompleteHeader")
                .setMessageAttribute("updateEmailWithVerificationCompleteBody")
                .createForm("update-email-with-verification-complete.ftl")
    }

    override fun canUseTokenRepeatedly(token: UpdateEmailWithVerificationActionToken?, tokenContext: ActionTokenContext<UpdateEmailWithVerificationActionToken>?): Boolean {
        return false
    }

    private fun LoginFormsProvider.setMessageAttribute(message: String, vararg parameters: String): LoginFormsProvider {
        this.setAttribute(message, this.getMessage(message, *parameters))
        return this
    }

    private operator fun List<Cookie>.get(name: String): Cookie? = find { it.name == name }
}
