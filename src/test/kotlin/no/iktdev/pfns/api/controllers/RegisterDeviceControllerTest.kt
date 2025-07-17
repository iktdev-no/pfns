package no.iktdev.pfns.api.controllers

import no.iktdev.pfns.TestBaseWithDatabase
import no.iktdev.pfns.api.objects.RegisterDeviceObject
import no.iktdev.pfns.api.table.RegisteredDevices
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*

class RegisterDeviceControllerTest : TestBaseWithDatabase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @BeforeEach
    fun resetTable() {
        transaction { RegisteredDevices.deleteAll() }
    }

    @Test
    fun `registerClient returns 200 OK on success`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity("rcv1", headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/register/receiver",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty()
    }

    @Test
    fun `registerClient returns 406 NOT_ACCEPTABLE on failure`() {
        // Simuler feil ved å mocke RegisteredDevices, men her kan vi sende ugyldig data
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity("\"\"", headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/register/receiver",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
        assertThat(response.body).isEqualTo("FCM Receiver ID cannot be blank.")
    }

    @Test
    fun `CORS filter sets Access-Control-Allow-Origin for valid Origin`() {
        val headers = HttpHeaders()
        headers.origin = "https://web.pfns.iktdev.no"
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val entity = HttpEntity("rcv1", headers)

        val response = restTemplate.exchange(
            "/api/fcm/register/receiver",
            HttpMethod.OPTIONS,  // Simulerer preflight
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers["Access-Control-Allow-Origin"]).contains("https://web.pfns.iktdev.no")
        assertThat(response.headers["Access-Control-Allow-Credentials"]).contains("true")
        assertThat(response.headers["Access-Control-Allow-Methods"]).contains("GET, POST, PUT, DELETE, OPTIONS")
    }

    @Test
    fun `CORS filter sets Access-Control-Allow-Origin for a random Origin`() {
        val headers = HttpHeaders()
        headers.origin = "https://potetmos.no"
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val entity = HttpEntity("rcv1", headers)

        val response = restTemplate.exchange(
            "/api/fcm/register/receiver",
            HttpMethod.OPTIONS,  // Simulerer preflight
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers["Access-Control-Allow-Origin"]).contains("https://potetmos.no")
        assertThat(response.headers["Access-Control-Allow-Credentials"]).contains("true")
        assertThat(response.headers["Access-Control-Allow-Methods"]).contains("GET, POST, PUT, DELETE, OPTIONS")
    }

}