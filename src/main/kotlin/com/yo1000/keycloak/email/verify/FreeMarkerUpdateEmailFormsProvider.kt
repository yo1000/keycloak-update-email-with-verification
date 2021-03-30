package com.yo1000.keycloak.email.verify

import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider
import org.keycloak.models.KeycloakSession
import org.keycloak.theme.FreeMarkerUtil
import org.keycloak.theme.Theme
import java.util.*
import javax.ws.rs.core.Response

class FreeMarkerUpdateEmailFormsProvider(
        session: KeycloakSession,
        freeMarker: FreeMarkerUtil
) : FreeMarkerLoginFormsProvider(
        session,
        freeMarker
) {
    companion object {
        const val ATTR_NAME_OVERRODE_TEMPLATE_NAME = "overrodeTemplateName"
    }

    override fun processTemplate(theme: Theme?, templateName: String?, locale: Locale?): Response {
        val overrodeTemplateName: Any? = attributes[ATTR_NAME_OVERRODE_TEMPLATE_NAME]

        return if (overrodeTemplateName != null && overrodeTemplateName is String && overrodeTemplateName.isNotEmpty()) {
            super.processTemplate(theme, overrodeTemplateName, locale)
        } else {
            super.processTemplate(theme, templateName, locale)
        }
    }
}
