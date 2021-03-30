package com.yo1000.keycloak.email.verify

import org.keycloak.Config
import org.keycloak.authentication.InitiatedActionSupport
import org.keycloak.authentication.RequiredActionContext
import org.keycloak.authentication.RequiredActionFactory
import org.keycloak.authentication.RequiredActionProvider
import org.keycloak.common.util.Time
import org.keycloak.email.EmailTemplateProvider
import org.keycloak.events.EventType
import org.keycloak.models.*
import org.keycloak.models.UserModel.RequiredAction
import org.keycloak.services.resources.LoginActionsService
import org.keycloak.theme.FreeMarkerUtil
import org.keycloak.theme.Theme
import java.net.URI
import java.time.Duration
import java.util.*
import javax.ws.rs.core.UriInfo

class UpdateEmailWithVerification : RequiredActionProvider, RequiredActionFactory {
    override fun initiatedActionSupport(): InitiatedActionSupport {
        return InitiatedActionSupport.SUPPORTED
    }

    override fun evaluateTriggers(context: RequiredActionContext) {}

    override fun requiredActionChallenge(context: RequiredActionContext) {
        val accessCode: String = context.generateCode()
        context.challenge(FreeMarkerUpdateEmailFormsProvider(context.session, FreeMarkerUtil())
                .setAuthenticationSession(context.authenticationSession)
                .setUser(context.user)
                .setActionUri(context.getActionUrl(accessCode))
                .setExecution(id)
                .setClientSessionCode(accessCode)
                .setAttribute(
                        FreeMarkerUpdateEmailFormsProvider.ATTR_NAME_OVERRODE_TEMPLATE_NAME,
                        "update-email-with-verification.ftl")
                .createResponse(RequiredAction.UPDATE_PROFILE)
        )
    }

    override fun processAction(context: RequiredActionContext) {
        val event = context.event
        event.event(EventType.UPDATE_PROFILE)

        val formData = context.httpRequest.decodedFormParameters
        val session: KeycloakSession = context.session
        val realm: RealmModel = context.realm
        val uriInfo: UriInfo = context.uriInfo
        val user: UserModel = context.user
        val oldEmail: String = user.email
        val newEmail: String = formData.getFirst("email")
        val maxAge: Int = realm.getActionTokenGeneratedByUserLifespan(UpdateEmailWithVerificationActionToken.TOKEN_TYPE)

        val actionToken: UpdateEmailWithVerificationActionToken = UpdateEmailWithVerificationActionToken(
                context.user.id,
                Time.currentTime() + maxAge,
                newEmail,
                oldEmail
        )
        val actionTokenUrl: String = LoginActionsService.actionTokenProcessor(uriInfo)
                .queryParam(Constants.KEY, actionToken.serialize(session, realm, uriInfo))
                .queryParam(Constants.EXECUTION, id)
                .queryParam(Constants.CLIENT_ID, context.authenticationSession.client.clientId)
                .queryParam(Constants.TAB_ID, context.authenticationSession.tabId)
                .build(realm.name)
                .toString()

        context.sendFreeMakerEmail(newEmail, actionTokenUrl, maxAge)
        context.success()
    }

    override fun getId(): String = "UPDATE_EMAIL_WITH_VERIFICATION"

    override fun getDisplayText(): String = "Update Email with Verification"

    override fun init(scope: Config.Scope) {}

    override fun postInit(factory: KeycloakSessionFactory) {}

    override fun create(keycloakSession: KeycloakSession): RequiredActionProvider {
        return this
    }

    override fun close() {}

    private fun RequiredActionContext.sendFreeMakerEmail(newEmail: String, actionUrl: String, maxAge: Int) {
        val linkExpiration: Duration = Duration.ofSeconds(maxAge.toLong())
        val locale: Locale = session.context.resolveLocale(user)
        val messageProps: Properties = session.theme().getTheme(Theme.Type.EMAIL).getMessages(locale);

        session.getProvider(EmailTemplateProvider::class.java)
                .setAuthenticationSession(session.context.authenticationSession)
                .setRealm(realm)
                .setUser(user.let {
                    object : UserModel by it {
                        override fun getEmail(): String = newEmail
                    }
                })
                .send("updateEmailWithVerificationSubject", "update-email-with-verification.ftl", mapOf(
                        "username" to user.username,
                        "email" to newEmail,
                        "link" to actionUrl,
                        "linkExpiration" to when {
                            linkExpiration.toDays() > 0 ->
                                "${linkExpiration.toDays()}${messageProps["linkExpirationFormatter.timePeriodUnit.days"]}"
                            linkExpiration.toHours() > 0 ->
                                "${linkExpiration.toHours()}${messageProps["linkExpirationFormatter.timePeriodUnit.hours"]}"
                            linkExpiration.toMinutes() > 0 ->
                                "${linkExpiration.toMinutes()}${messageProps["linkExpirationFormatter.timePeriodUnit.minutes"]}"
                            else ->
                                "${linkExpiration.seconds}${messageProps["linkExpirationFormatter.timePeriodUnit.seconds"]}"
                        }
                ))
    }

    private val URI.rawPathWithTrailingSlash get(): String {
        return if (rawPath.endsWith("/"))
            rawPath
        else
            "$rawPath/"
    }
}
