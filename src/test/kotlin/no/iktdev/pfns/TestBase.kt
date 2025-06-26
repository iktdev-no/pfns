package no.iktdev.pfns

import no.iktdev.pfns.token.ApiTokenService
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI
import java.net.http.HttpHeaders

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension::class)
class TestBase {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var apiTokenService: ApiTokenService

    @Bean
    fun testRestTemplate(): TestRestTemplate {
        val baseUrl = URI("http://localhost:$port")
        return TestRestTemplate(RestTemplateBuilder().rootUri(baseUrl.toString()))
    }

    private var apiAuthorizationToken: String = ""
    fun org.springframework.http.HttpHeaders.addAuthorizationBearer() {
        if (apiAuthorizationToken.isBlank()) {
            // Fetch the API token from the service
            apiAuthorizationToken = apiTokenService.createApiToken("srv1", "test@test.no", "127.0.0.1")!!
        }
        this.add("Authorization", "Bearer ${apiAuthorizationToken}")

    }
}