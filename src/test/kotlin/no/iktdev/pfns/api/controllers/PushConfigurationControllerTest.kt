package no.iktdev.pfns.api.controllers

import com.google.firebase.FirebaseApp
import no.iktdev.pfns.TestBaseWithDatabase
import no.iktdev.pfns.api.objects.RemoteServer
import no.iktdev.pfns.api.objects.RemoteServerObject
import no.iktdev.pfns.api.table.RegisteredDevices
import no.iktdev.pfns.token.ApiTokenService
import no.iktdev.pfns.token.UserTokenService
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.*

class PushConfigurationControllerTest : TestBaseWithDatabase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @MockBean
    lateinit var firebaseService: no.iktdev.pfns.api.services.FirebaseService

    val remoteServer = RemoteServer(
        id = "srv1",
        name = "Example Server",
        fingerprint = UUID.randomUUID().toString(),
        lan = "http://example.lan",
        remote = "https://testserver.example.com",
        remoteSecure = false
    )

    @BeforeEach
    fun setup() {
        transaction { RegisteredDevices.deleteAll() }
        Mockito.reset(firebaseService)
    }


    @Test
    fun `sendServerConfiguration returns 403 FORBIDDEN if not registered`() {
        val data = RemoteServerObject(serverId = "srv1", receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `sendServerConfiguration returns 200 OK if registered and firebaseApp is not null`() {
        transaction {
            RegisteredDevices.storeApiTokenOrUpdate("srv1", "rcv1", "127.0.0.1")
        }
        Mockito.`when`(firebaseService.firebaseApp).thenReturn(Mockito.mock(FirebaseApp::class.java))

        Mockito.`when`(firebaseService.sendMessage(any())).thenReturn(true)

        val data = RemoteServerObject(serverId = "srv1", receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()

        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `sendServerConfiguration returns 420 METHOD_FAILURE if firebaseApp is null`() {
        transaction {
            RegisteredDevices.storeApiTokenOrUpdate("srv1", "rcv1", "127.0.0.1")
        }
        Mockito.`when`(firebaseService.firebaseApp).thenReturn(null)

        val data = RemoteServerObject(serverId = "srv1", receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()

        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode.value()).isEqualTo(420)
    }

    @Test
    fun `sendServerConfiguration returns 403 FORBIDDEN if serverId does not match`() {
        // Register with a different serverId
        transaction {
            RegisteredDevices.storeApiTokenOrUpdate("srv2", "rcv1", "127.0.0.1")
        }
        val data = RemoteServerObject(serverId = "srv1", receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()

        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `sendServerConfiguration returns 200 OK if only receiverId is registered and serverId is null`() {
        // Register device with only receiverId (no serverId)
        transaction {
            RegisteredDevices.storeApiTokenOrUpdate(null, "rcv1", "127.0.0.1")
        }
        Mockito.`when`(firebaseService.firebaseApp).thenReturn(Mockito.mock(FirebaseApp::class.java))
        Mockito.`when`(firebaseService.sendMessage(any())).thenReturn(true)

        val data = RemoteServerObject(serverId = null, receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()

        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `sendServerConfiguration returns 403 FORBIDDEN if only receiverId is provided but device is registered with both serverId and receiverId`() {
        // Register device with both serverId and receiverId
        transaction {
            RegisteredDevices.storeApiTokenOrUpdate("srv1", "rcv1", "127.0.0.1")
        }
        Mockito.`when`(firebaseService.firebaseApp).thenReturn(Mockito.mock(FirebaseApp::class.java))
        Mockito.`when`(firebaseService.sendMessage(any())).thenReturn(true)

        val data = RemoteServerObject(serverId = null, receiverId = "rcv1", server = remoteServer)
        val headers = HttpHeaders()
        headers.addAuthorizationBearer()

        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(data, headers)

        val response = restTemplate.postForEntity(
            "/api/fcm/config/server",
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

}
