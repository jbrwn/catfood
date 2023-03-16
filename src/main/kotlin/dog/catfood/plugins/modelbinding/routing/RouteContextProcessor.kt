package dog.catfood.plugins.modelbinding.routing

import dog.catfood.plugins.controllers.routesHelper
import dog.catfood.plugins.modelbinding.ContextProcessor
import io.ktor.server.application.ApplicationCall

class RouteContextProcessor: ContextProcessor {
    override fun process(call: ApplicationCall): Map<String, Any?> {
        return mapOf("routes" to call.routesHelper)
    }
}
