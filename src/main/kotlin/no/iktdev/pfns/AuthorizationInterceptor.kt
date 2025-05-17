package no.iktdev.pfns

import mu.KotlinLogging
import no.iktdev.pfns.services.AuthenticationService
import no.iktdev.pfns.services.TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.run

@Component
@Order(1)
class AuthorizationInterceptor(
    @Autowired val authenticationService: AuthenticationService
): HandlerInterceptor {
    val log = KotlinLogging.logger {}

    init {
        log.info { "Loading ${this.javaClass.simpleName}" }
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val mode = getAuthenticationDefined(request, handler)
        if (mode == Mode.None) return true
        val token = request.getAuthorization() ?: run {
            response.status = HttpStatus.UNAUTHORIZED.value()
            return false
        }
        if (!authenticationService.isValid(token)) {
            response.status = HttpStatus.BAD_REQUEST.value()
            return false
        }
        return true
    }

    fun getAuthenticationDefined(request: HttpServletRequest, handler: Any): Mode {
        val validation = try {
            if (handler is HandlerMethod)
                handler.method.getAnnotation(RequiresAuthentication::class.java)
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            val url = request.requestURL.toString()
            val queryParams = request.queryString
            val body = request.reader.lines().collect(Collectors.joining(System.lineSeparator()))
            log.error { "Error report:\n\tSource:${request.getRequestersIp()}\n\tUrl:$url\n\tQuery params:$queryParams\n\tBody:$body" }
            null
        }
        return validation?.mode ?: run {
            log.warn { "No handler found on ${request.method} @ ${request.requestURI}" }
            Mode.None
        }
    }
}

fun HttpServletRequest?.getAuthorization(): String? {
    return this?.getHeader("Authorization")
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAuthentication(val mode: Mode = Mode.Strict)

enum class Mode {
    Strict,
    None
}

