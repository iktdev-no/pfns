package no.iktdev.pfns

import mu.KotlinLogging
import no.iktdev.pfns.interceptor.WebAuthorizationInterceptor
import no.iktdev.pfns.api.table.ApiToken
import no.iktdev.pfns.api.table.RegisteredDevices
import no.iktdev.pfns.database.MySqlDataSource
import no.iktdev.pfns.interceptor.ApiAuthorizationInterceptor
import no.iktdev.pfns.web.tables.UserRefreshToken
import no.iktdev.pfns.web.tables.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import kotlin.system.exitProcess

val log = KotlinLogging.logger {}

@SpringBootApplication
class Application {
}

val tables: Array<Table> = arrayOf(
    UserTable,
    UserRefreshToken,
    ApiToken,
    RegisteredDevices
)

fun databaseSetup(database: Database) {
    transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*tables)
        log.info("Database setup completed")
    }
}


var context: ApplicationContext? = null
fun main(args: Array<String>) {
    val datasource = MySqlDataSource.fromDatabaseEnv()
    val database = datasource.createDatabase().also { x ->
        println(x)
    } ?: throw RuntimeException("Unable to create database..")


    databaseSetup(database)
    context = runApplication<Application>(*args)

    if (Env.targetApplicationPackageName.isNullOrBlank()) {
        log.error { "Target application package name is not defined!" }
        exitProcess(1)
    }
}

@Configuration
class InterceptorConfiguration(
    @Autowired val authInterceptor: WebAuthorizationInterceptor,
    @Autowired val apiAuthInterceptor: ApiAuthorizationInterceptor
): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        log.info { "Adding AuthorizationInterceptor" }
        registry.addInterceptor(authInterceptor).addPathPatterns("/webapi/**")
        registry.addInterceptor(apiAuthInterceptor).addPathPatterns("/api/**")
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
            .allowedOriginPatterns("https://*.iktdev.no")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
            .allowCredentials(true)
    }
}

