package no.iktdev.pfns.web.controller

import no.iktdev.pfns.TestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

class LogoutUserControllerTest: TestBase() {


    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `logout deletes refresh token cookie`() {
        val headers = HttpHeaders()
        headers["Cookie"] = "refreshToken=valid-refresh-token"
        val entity = HttpEntity<String>(null, headers)

        val response = restTemplate.exchange(
            "/webapi/auth/logout",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val setCookie = response.headers["Set-Cookie"]?.find { it.startsWith("refreshToken=") }
        assertThat(setCookie).isNotNull
        assertThat(setCookie).contains("Max-Age=0")
    }

    @Test
    fun `logout without refresh token cookie still returns ok`() {
        val entity = HttpEntity<String>(null, HttpHeaders())

        val response = restTemplate.exchange(
            "/webapi/auth/logout",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val setCookie = response.headers["Set-Cookie"]?.find { it.startsWith("refreshToken=") }
        assertThat(setCookie).isNotNull
        assertThat(setCookie).contains("Max-Age=0")
    }
}