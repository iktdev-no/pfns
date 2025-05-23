package no.iktdev.pfns.token

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import no.iktdev.pfns.Env
import no.iktdev.pfns.InstantFrom
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.TokenPair
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class UserTokenService: TokenImpl(Env.webTokenSecret) {

    fun createTokenPair(email: String): TokenPair {
        val refreshToken = UUID.randomUUID()
        val expiry = InstantFrom(Env.timeZone).plus(30, ChronoUnit.DAYS)
        val jit = UserRefreshToken.storeRefreshToken(email, refreshToken.toString(), expiry)
        return TokenPair(
            refreshToken = refreshToken.toString(),
            accessToken = createAccessToken(jit, email)
        )
    }


    private fun createAccessToken(refreshTokenId: String, email: String): String {
        return JWT.create()
            .withJWTId(refreshTokenId)
            .withIssuer(issuer)
            .withIssuedAt(InstantFrom(Env.timeZone))
            .withSubject(email)
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
            .sign(algorithm())
    }

    override fun isValid(token: DecodedJWT): Boolean {
        val isValid = super.isValid(token)
        if (!isValid) return false
        val jit = token.id
        return UserRefreshToken.isRefreshTokenValid(jit)
    }




}