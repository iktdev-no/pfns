package no.iktdev.pfns.web.userAccess

import no.iktdev.pfns.UserDisabledException
import no.iktdev.pfns.web.tables.User
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.UserService
import no.iktdev.pfns.web.TokenPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.jvm.Throws

@Service
class UserAuthenticationService(
    @Autowired val userService: UserService,
    @Autowired val googleAuthenticationService: GoogleAuthenticationService
) {

    @Throws(UserDisabledException::class)
    fun postAuthenticationOnLogin(user: AuthenticatedUser): TokenPair? {
        val tokens = userService.loginUser(user)
        return tokens
    }

    @Throws(UserDisabledException::class)
    fun getGoogleAuthenticated(token: String): TokenPair? {
        val user = googleAuthenticationService.getUserInfo(token) ?: return null
        return postAuthenticationOnLogin(user)
    }

    fun getUserFromToken(token: String): User? {
        return userService.getUserOn(token)
    }

    fun getNewTokens(refreshToken: String): TokenPair? {
        val canCreateNewAccessToken = UserRefreshToken.refreshTokenExistsAndIsValid(refreshToken)
        if (canCreateNewAccessToken) {
            val email = userService.getEmailOnRefreshToken(refreshToken) ?: run {
                return null
            }
            return userService.tokenService.createTokenPair(email)
        }
        return null
    }

    fun isAccessTokenValid(accessToken: String): Boolean {
        return userService.tokenService.isValid(accessToken)
    }

}