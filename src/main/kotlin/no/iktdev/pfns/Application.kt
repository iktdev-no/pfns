package no.iktdev.pfns

import mu.KotlinLogging
import no.iktdev.pfns.database.MySqlDataSource
import no.iktdev.pfns.interceptor.AuthorizationInterceptor
import no.iktdev.pfns.api.table.ApiToken
import no.iktdev.pfns.api.table.RegisteredDevices
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.tables.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import kotlin.system.exitProcess

val log = KotlinLogging.logger {}

@EnableScheduling
@SpringBootApplication
class Application {
}

val tables: Array<Table> = arrayOf(
    UserTable,
    UserRefreshToken,
    ApiToken,
    RegisteredDevices
)


var context: ApplicationContext? = null
fun main(args: Array<String>) {
    val dataSource = MySqlDataSource.fromDatabaseEnv()
    val database = dataSource.createDatabase().also { x ->
        println(x)
        transaction(x) {
            SchemaUtils.createMissingTablesAndColumns(*tables)
            log.info("Database transaction completed")
        }
    }

    context = runApplication<Application>(*args)
    if (Env.targetApplicationPackageName.isNullOrBlank()) {
        log.error { "Target application package name is not defined!" }
        exitProcess(1)
    }
}

@Configuration
class InterceptorConfiguration(
    @Autowired val authInterceptor: AuthorizationInterceptor
): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        log.info { "Adding AuthorizationInterceptor" }
        registry.addInterceptor(authInterceptor).addPathPatterns("/**")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(object: PathResourceResolver() {
                override fun getResource(resourcePath: String, location: Resource): Resource? {
                    // Show index.html if no resource was found
                    return if (!location.createRelative(resourcePath).exists() && !location.createRelative(resourcePath).isReadable) {
                        location.createRelative("index.html");
                    } else {
                        location.createRelative(resourcePath);
                    }
                }
            })
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("localhost", "http://localhost:3000", "localhost:80")
            .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
            .allowCredentials(true)
    }
}

