package dog.catfood.plugins.modelbinding.controllers

import dog.catfood.plugins.controllers.actionName
import dog.catfood.plugins.controllers.controllerName
import io.ktor.server.application.ApplicationCall

fun controllerActionPathResolver(call: ApplicationCall): String {
    return "/${call.controllerName}/${call.actionName}".lowercase()
}
