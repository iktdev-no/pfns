package no.iktdev.pfns.interceptor

import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest

abstract class AuthorizationInterceptorImpl(): HandlerInterceptor {


    abstract fun getAuthenticationMode(request: HttpServletRequest, handler: Any): Mode


    fun HttpServletRequest?.getAuthorization(): String? {
        return this?.getHeader("Authorization")
    }

}

