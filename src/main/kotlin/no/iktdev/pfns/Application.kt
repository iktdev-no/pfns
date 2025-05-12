package no.iktdev.pfns

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class Application {
}

var context: ApplicationContext? = null
fun main(args: Array<String>) {
    context = runApplication<Application>(*args)
}