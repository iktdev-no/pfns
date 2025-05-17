package no.iktdev.pfns

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.system.exitProcess

val log = KotlinLogging.logger {}

@EnableScheduling
@SpringBootApplication
class Application {
}

var context: ApplicationContext? = null
fun main(args: Array<String>) {
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
}

