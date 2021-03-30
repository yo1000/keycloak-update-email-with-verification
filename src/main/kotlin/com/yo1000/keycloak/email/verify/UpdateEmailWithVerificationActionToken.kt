package com.yo1000.keycloak.email.verify

import com.fasterxml.jackson.annotation.JsonProperty
import org.keycloak.authentication.actiontoken.DefaultActionToken
import java.util.*

class UpdateEmailWithVerificationActionToken(
        userId: String?,
        absoluteExpirationInSecs: Int,
        @field:JsonProperty("newEmail")
        var newEmail: String?,
        @field:JsonProperty("oldEmail")
        var oldEmail: String?
) : DefaultActionToken(
        userId,
        TOKEN_TYPE,
        absoluteExpirationInSecs,
        UUID.randomUUID()
) {
    companion object {
        const val TOKEN_TYPE = "update-email-with-verification"
    }

    @Suppress("unused")
    private constructor() : this(null, 0, null, null) {
        // !! Don't REMOVE !! You must have this private constructor for deserializer
    }
}
