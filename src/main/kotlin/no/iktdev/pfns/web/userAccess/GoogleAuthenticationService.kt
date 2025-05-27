package no.iktdev.pfns.web.userAccess

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import mu.KotlinLogging
import no.iktdev.pfns.Env
import org.springframework.stereotype.Service

@Service
class GoogleAuthenticationService {
    val log = KotlinLogging.logger {}

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = NetHttpTransport()

    private val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(listOf(Env.googleAuthenticationClientId))
        .build()

    fun getUserInfo(token: String): AuthenticatedUser? {
        val payload = validate(token) ?: run {
            log.warn { "Google ID token failed validation: $token" }
            return null
        }
        return AuthenticatedUser(
            oAuthUserId = payload.subject,
            name = payload.get("name")?.let { value: Any? ->
                value as? String
                    ?: if (value == null) { payload.email } else {
                        log.warn { "Attempting to set name for ${payload.subject}" }
                        value.toString()
                    }
            } as String,
            email = payload.email
        )
    }

    private fun validate(token: String): GoogleIdToken.Payload? {
        val idToken = try {
            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
        return idToken?.payload
    }
}