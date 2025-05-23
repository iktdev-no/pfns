package no.iktdev.pfns.web.controller

import no.iktdev.pfns.api.ApiToken
import no.iktdev.pfns.api.ExposableApiTokenObject
import no.iktdev.pfns.getRequestersIp
import no.iktdev.pfns.interceptor.Mode
import no.iktdev.pfns.interceptor.RequiresWebAuthentication
import no.iktdev.pfns.token.ApiTokenService
import no.iktdev.pfns.web.userAccess.UserAuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/web/token")
class ApiTokensController(
    @Autowired val authentication: UserAuthenticationService,
    @Autowired val apiTokenService: ApiTokenService
) {


    @RequiresWebAuthentication(Mode.Strict)
    @PostMapping("/create")
    fun generateTokenForServer(@RequestHeader("Authorization") token: String, @RequestBody serverId: String, request: HttpServletRequest): ResponseEntity<String?> {
        val email = authentication.getUserFromToken(token)?.email ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        val token = apiTokenService.createApiToken(serverId, email, request.getRequestersIp() ?: "unknown")
        return if (token == null || token.isBlank()) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } else {
            ResponseEntity.ok(token)
        }
    }

    @RequiresWebAuthentication(Mode.Strict)
    @GetMapping("/all")
    fun getServerApiTokens(@RequestHeader("Authorization") token: String): ResponseEntity<List<ExposableApiTokenObject>> {
        val user = authentication.getUserFromToken(token) ?: return ResponseEntity.status(401).build()
        val myTokens = ApiToken.getMyTokens(user.email).map { it.toExposable() }
        return ResponseEntity.ok(myTokens)
    }

    @RequiresWebAuthentication(Mode.Strict)
    @DeleteMapping()
    fun deleteTokenForServer(@RequestHeader("Authorization") token: String, @RequestBody serverId: String, request: HttpServletRequest): ResponseEntity<Boolean> {
        val email = authentication.getUserFromToken(token)?.email ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        val success = apiTokenService.deleteToken(serverId, email)
        return ResponseEntity.ok(success)
    }
}