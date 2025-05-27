package no.iktdev.pfns.web.controller

import no.iktdev.pfns.Env
import no.iktdev.pfns.InstantFrom
import no.iktdev.pfns.TestBaseWithDatabase
import no.iktdev.pfns.token.UserTokenService
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.tables.UserTable
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.temporal.ChronoUnit
import kotlin.collections.find

class UserControllerTest: TestBaseWithDatabase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
        lateinit var userTokenService: UserTokenService

    @BeforeEach
    fun resetTables() {
        transaction {
            UserRefreshToken.deleteAll()
            UserTable.deleteAll()
        }
    }

    val email = "test@pfns.test"

    @Test
    fun `returns 401 when both tokens are invalid`() {
        val headers = HttpHeaders()
        headers["Authorization"] = "invalid"
        headers["Cookie"] = "refreshToken=invalid"
        val entity = HttpEntity<String>(null, headers)

        val response = restTemplate.exchange(
            "/api/web/auth/login",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `returns 200 and access token when access token is valid`() {
        // Arrange: Insert a valid access token in your test setup
        UserTable.createUser("test", "Test", email)

        val (accessToken, refreshToken) = userTokenService.createTokenPair(email)

        val headers = HttpHeaders()
        headers["Authorization"] = accessToken
        headers["Cookie"] = "refreshToken=$refreshToken"
        val entity = HttpEntity<String>(null, headers)

        val response = restTemplate.exchange(
            "/api/web/auth/login",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        // Optionally, assert the returned token value
    }

    @Test
    fun `returns 200 and sets refresh cookie when refresh token is valid but access token is not`() {
        // Arrange: Insert a valid refresh token in your test setup

        val (accessToken, refreshToken) = userTokenService.createTokenPair(email)

        UserTable.createUser("test", "Test", email)
        UserRefreshToken.storeRefreshToken(
            email = email,
            token = refreshToken,
            expiresAt = InstantFrom(Env.timeZone).plus(30, ChronoUnit.DAYS)
        )

        val headers = HttpHeaders()
        headers["Authorization"] = "invalid"
        headers["Cookie"] = "refreshToken=$refreshToken"
        val entity = HttpEntity<String>(null, headers)

        val response = restTemplate.exchange(
            "/api/web/auth/login",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        val setCookie = response.headers["Set-Cookie"]?.find { it.startsWith("refreshToken=") }
        assertThat(setCookie).isNotNull
    }

    @Test
    fun `returns 401 and error message when user is disabled`() {
        // Arrange: Insert a disabled user and corresponding refresh token
        val (accessToken, refreshToken) = userTokenService.createTokenPair(email)


        UserRefreshToken.storeRefreshToken(
            email = email,
            token = refreshToken,
            expiresAt = InstantFrom(Env.timeZone).plus(30, ChronoUnit.DAYS)
        )

        UserTable.createUser("test", "Test", email)
        transaction {
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.disabled] = true
            }
        }


        val headers = HttpHeaders()
        headers["Authorization"] = "invalid"
        headers["Cookie"] = "refreshToken=$refreshToken"
        val entity = HttpEntity<String>(null, headers)

        val response = restTemplate.exchange(
            "/api/web/auth/login",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body).contains("Your user account is disabled")
    }

}