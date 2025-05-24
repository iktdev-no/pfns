package no.iktdev.pfns.web.controller

import mu.KotlinLogging
import no.iktdev.pfns.UserDisabledException
import no.iktdev.pfns.interceptor.Mode
import no.iktdev.pfns.interceptor.RequiresWebAuthentication
import no.iktdev.pfns.web.userAccess.UserAuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/web/auth")
class UserController(
    @Autowired val authentication: UserAuthenticationService
) {
    private val log = KotlinLogging.logger {}
    init {
        log.info("Loaded ${this::class.simpleName}")
    }

    private fun assignRefreshToken(refreshToken: String, response: HttpServletResponse) {
        val cookie = Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = false // true i prod (HTTPS)
            path = "/"
            maxAge = 30 * 24 * 60 * 60 // 30 dager
        }
        response.addCookie(cookie)
    }

    @RequiresWebAuthentication(Mode.None)
    @GetMapping("/logout")
    fun logout(response: HttpServletResponse) {
        val cookie = Cookie("refreshToken", "").apply {
            isHttpOnly = true
            secure = false // true i prod (HTTPS)
            path = "/"
            maxAge = 0
        }
        response.addCookie(cookie)
    }

    @RequiresWebAuthentication(Mode.None)
    @GetMapping("/login")
    fun loginWithBoth(
        @CookieValue("refreshToken") refreshToken: String,
        @RequestHeader("Authorization") accessToken: String,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        val isAccessTokenValid = authentication.isAccessTokenValid(accessToken)
        if (isAccessTokenValid) {
            return ResponseEntity.ok(authentication.userService.tokenService.afterBearer(accessToken))
        }
        try {
            val newToken = authentication.getNewTokens(refreshToken) ?: return ResponseEntity.status(401).build()
            assignRefreshToken(newToken.refreshToken, response)
            return ResponseEntity.ok(newToken.accessToken)
        } catch (e : UserDisabledException) {
            return ResponseEntity.status(401).body("Your user account is disabled")
        }
    }

    @RequiresWebAuthentication(Mode.None)
    @PostMapping("/login/google")
    fun authenticateGoogle(@RequestBody token: String, response: HttpServletResponse): ResponseEntity<String> {
        try {
            val result = authentication.getGoogleAuthenticated(token) ?: return ResponseEntity.status(401).build()
            // 🔐 Sett refresh token som HttpOnly cookie
            assignRefreshToken(result.refreshToken, response)
            return ResponseEntity.ok(result.accessToken);
        } catch (e : UserDisabledException) {
            return ResponseEntity.status(401).body("Your user account is disabled")
        }
    }

    @RequiresWebAuthentication(Mode.Strict)
    @GetMapping("/refresh")
    fun refreshTokens(@CookieValue("refreshToken") token: String, response: HttpServletResponse): ResponseEntity<String> {
        try {
            val newToken = authentication.getNewTokens(token) ?: return ResponseEntity.status(401).build()
            assignRefreshToken(newToken.refreshToken, response)
            return ResponseEntity.ok(newToken.accessToken)
        } catch (e : UserDisabledException) {
            return ResponseEntity.status(401).body("Your user account is disabled")
        }
    }
}