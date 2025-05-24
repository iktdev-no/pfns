package no.iktdev.pfns.token

import com.auth0.jwt.JWT
import no.iktdev.pfns.Env
import no.iktdev.pfns.InstantFrom
import no.iktdev.pfns.api.table.ApiToken
import org.springframework.stereotype.Service

@Service
class ApiTokenService(): TokenImpl(Env.apiTokenSecret) {


    fun getServerIdFrom(token: String): String? {
        return decode(token)?.subject
    }

    fun createApiToken(serverId: String, email: String, ip: String): String? {
        val token = JWT.create()
            .withIssuer(issuer)
            .withIssuedAt(InstantFrom(Env.timeZone))
            .withSubject(serverId)
            .sign(algorithm()).let {
                ApiToken.storeApiToken(serverId = serverId, email = email, ip = ip, token = it)
            }
        return token;
    }

    fun deleteToken(serverId: String, email: String): Boolean {
        return ApiToken.deleteApiToken(serverId, email)
    }

}
