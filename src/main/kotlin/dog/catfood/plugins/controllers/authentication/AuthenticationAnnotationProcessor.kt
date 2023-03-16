package dog.catfood.plugins.controllers.authentication

import dog.catfood.plugins.controllers.AnnotationProcessor
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

class AuthenticationAnnotationProcessor: AnnotationProcessor<Authenticate>(Authenticate::class) {
    override fun process(annotation: Authenticate, route: Route): Route {
        val configuration = annotation.configuration
        return route.authenticate(configuration, build = { })
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Authenticate(val configuration: String)