package com.yo1000.keycloak.email.verify

import org.keycloak.Config
import org.keycloak.forms.login.LoginFormsProvider
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.Urls
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resource.RealmResourceProviderFactory
import org.keycloak.services.resources.AbstractSecuredLocalService
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ExtRealmResourceProvider(
        private val keycloakSession: KeycloakSession
) : RealmResourceProvider {
    override fun getResource(): Any {
        return object : AbstractSecuredLocalService(
                keycloakSession.context.realm,
                keycloakSession.context.client
        ) {
            override fun getValidPaths(): Set<String> = this::class.java.methods
                    .mapNotNull { it.getAnnotation(Path::class.java)?.value }
                    .toSet()

            override fun getBaseRedirectUri(): URI = Urls
                    .accountBase(session.context.uri.baseUri)
                    .path("/")
                    .build(realm.name)

            @GET
            @Path("accept-email-updates")
            @Produces(MediaType.TEXT_PLAIN)
            fun getAcceptEmailUpdates(): Response {
                return keycloakSession.getProvider(LoginFormsProvider::class.java)
                        .setAuthenticationSession(keycloakSession.context.authenticationSession)
                        .setMessageAttribute("updateEmailWithVerificationChallengeHeader")
                        .setMessageAttribute("updateEmailWithVerificationChallengeBody")
                        .createForm("update-email-with-verification-challenge.ftl")
            }
        }
    }

    override fun close() {}

    private fun LoginFormsProvider.setMessageAttribute(message: String, vararg parameters: String): LoginFormsProvider {
        this.setAttribute(message, this.getMessage(message, *parameters))
        return this
    }
}

class ExtRealmResourceProviderFactory : RealmResourceProviderFactory {
    override fun getId(): String = "ext"

    override fun create(session: KeycloakSession): RealmResourceProvider {
        return ExtRealmResourceProvider(session)
    }

    override fun init(p0: Config.Scope?) {}

    override fun postInit(p0: KeycloakSessionFactory?) {}

    override fun close() {}
}