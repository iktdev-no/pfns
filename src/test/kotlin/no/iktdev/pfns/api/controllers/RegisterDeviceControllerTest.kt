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
}