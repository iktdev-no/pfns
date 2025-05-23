package no.iktdev.pfns.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT

abstract class TokenImpl(val secret: String) {
    val issuer = "Proxy for Firebase Notification Service"

    fun algorithm(): Algorithm {
        return Algorithm.HMAC256(secret) ?: throw MissingConfigurationException("HS256 JWT secret is not provided correctly, clear environment variable to use default...")
    }
    open val verifier = JWT.require(algorithm()).withIssuer(issuer).build()


    protected fun hasBearer(token: String) = token.contains("Bearer")

    fun afterBearer(token: String) = token.substringAfter("Bearer").trim()

    open fun isValid(token: DecodedJWT): Boolean {
        return try {
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    open fun isValid(token: String): Boolean {
        return decode(token)?.let { j -> isValid(j) } ?: false
    }

    fun decode(token: String): DecodedJWT? {
        return JWT.decode(afterBearer(token))
    }
}

class MissingConfigurationException(message: String): Exception(message)
