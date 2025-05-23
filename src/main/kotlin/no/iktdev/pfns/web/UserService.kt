package no.iktdev.pfns.web

import no.iktdev.pfns.UserDisabledException
import no.iktdev.pfns.token.UserTokenService
import no.iktdev.pfns.web.userAccess.AuthenticatedUser
import no.iktdev.pfns.web.tables.User
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.tables.UserTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(
    @Autowired val tokenService: UserTokenService
) {

    fun loginUser(user: AuthenticatedUser): TokenPair? {
        if (!UserTable.doesUserExist(user.email)) {
            UserTable.createUser(user.oAuthUserId, user.name, user.email)
        }
        val user = UserTable.getUser(user.email) ?: return null
        if (user.disabled) {
            throw UserDisabledException()
        }
        return generateNewTokens(email = user.email)
    }

    fun generateNewTokens(email: String): TokenPair? {
        return tokenService.createTokenPair(email)
    }

    fun getUserOn(token: String): User? {
        val email = tokenService.decode(token)?.subject ?: return null
        return UserTable.getUser(email)
    }

    fun getEmailOnRefreshToken(refreshToken: String): String? {
        return UserRefreshToken.getEmailFromRefreshToken(refreshToken)
    }
}