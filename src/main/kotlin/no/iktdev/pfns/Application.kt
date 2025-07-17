package no.iktdev.pfns

import mu.KotlinLogging
import no.iktdev.pfns.interceptor.WebAuthorizationInterceptor
import no.iktdev.pfns.api.table.ApiToken
import no.iktdev.pfns.api.table.DeviceIdentifiers
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
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.system.exitProcess

val log = KotlinLogging.logger {}

@SpringBootApplication
class Application {
}

val tables: Array<Table> = arrayOf(
    UserTable,
    UserRefreshToken,
    ApiToken,
    RegisteredDevices,
    DeviceIdentifiers,
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
        registry.addMapping("/webapi/**")
            .allowedOriginPatterns("https://*.iktdev.no")
            .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
            .allowCredentials(true)
    }
}


@Component
class DynamicCorsFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")

        // 🔍 Bare bruk CORS hvis path starter med /api/
        val path = request.requestURI
        if (path.startsWith("/api/") && origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
            response.setHeader("Access-Control-Allow-Credentials", "true")
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")
        }

        // OPTIONS-preflight håndteres separat
        if (path.startsWith("/api/") && request.method == "OPTIONS") {
            response.status = HttpServletResponse.SC_OK
            return
        }

        filterChain.doFilter(request, response)
    }
}