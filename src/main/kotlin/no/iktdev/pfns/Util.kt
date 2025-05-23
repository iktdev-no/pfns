package no.iktdev.pfns

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest

fun InstantFrom(id: ZoneId): Instant {
    return ZonedDateTime.now(id).toInstant()
}

fun HttpServletRequest?.getRequestersIp(): String? {
    this ?: return null
    val xforwardedIp: String? = this.getHeader("X-Forwarded-For")
    return if (xforwardedIp.isNullOrEmpty()) {
        this.remoteAddr
    } else xforwardedIp
}

class UserDisabledException(override val message: String = "User is disabled"): Exception()