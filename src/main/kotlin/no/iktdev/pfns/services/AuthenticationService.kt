package no.iktdev.pfns.services

import mu.KotlinLogging
import no.iktdev.pfns.tables.AccessToken
import no.iktdev.pfns.tables.findValidAccessToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    @Autowired val tokenService: TokenService
) {
    val log = KotlinLogging.logger {}

    fun isValid(token: String): Boolean {
        if (!tokenService.isValid(token)) {
            log.error { "Token is not valid!" }
            return false
        }

        val serverId = tokenService.getServerIdFrom(token) ?: run {
            log.error { "No server id found in token" }
            return false
        }
        val storedToken = findValidAccessToken(serverId) ?: run {
            log.error { "Token not found in database!" }
            return false
        }

        if (storedToken.revoked) {
            log.error { "Token is revoked" }
            return false
        }

        if (storedToken.token != tokenService.afterBearer(token)) {
            log.error { "Token passed is not the one stored!" }
            return false
        }

        return true
    }



}