package no.iktdev.pfns.api.controllers

import no.iktdev.pfns.interceptor.Mode
import no.iktdev.pfns.interceptor.RequiresApiAuthentication
import no.iktdev.pfns.token.ApiTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/token")
class TokenController(
    @Autowired val apiTokenService: ApiTokenService
) {

    @RequiresApiAuthentication(Mode.None)
    @PostMapping("/validate")
    fun tokenPath(request: HttpServletRequest): ResponseEntity<String> {
        val token = request.getHeader("Authorization") ?: run {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Authorization header provided")
        }
        if (!apiTokenService.isValid(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is not valid")
        }
        return ResponseEntity.ok().body("Ok")
    }

}