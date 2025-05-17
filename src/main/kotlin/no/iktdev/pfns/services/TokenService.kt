package no.iktdev.pfns.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import mu.KotlinLogging
import no.iktdev.pfns.Env
import no.iktdev.pfns.InstantFrom
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenService {
    private val log = KotlinLogging.logger {}

    companion object {
        fun algorithm(): Algorithm {
            return Algorithm.HMAC256(Env.jwtSecret) ?: throw MissingConfigurationException("HS256 JWT secret is not provided correctly, clear environment variable to use default...")
        }
        val issuer = "Proxy for Firebase Notification Service"
        val verifier = JWT.require(algorithm()).withIssuer(issuer).build()
    }

    private fun hasBearer(token: String) = token.contains("Bearer")

    fun afterBearer(token: String) = token.substringAfter("Bearer").trim()

    fun isValid(token: String): Boolean {
        return try {
            verifier.verify(afterBearer(token))
            true
        } catch (e: Exception) {
            false
        }
    }
    fun isValid(token: DecodedJWT): Boolean {
        return try {
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun decode(token: String): DecodedJWT? {
        return JWT.decode(afterBearer(token))
    }

    fun getServerIdFrom(token: String): String? {
        return decode(token)?.subject
    }

    fun createApiToken(serverId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withIssuedAt(InstantFrom(Env.timeZone))
            .withSubject(serverId)
            .sign(algorithm())
    }

}
class MissingConfigurationException(message: String): Exception(message)
