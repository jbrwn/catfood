package dog.catfood.controllers

import dog.catfood.models.isLoggedIn
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.modelbinding.respondView
import io.ktor.server.application.ApplicationCall

class HomeController: Controller {
    @GET("/")
    suspend fun index(call: ApplicationCall) {
        val loggedIn = call.isLoggedIn()
        call.respondView(viewData = mapOf("loggedIn" to loggedIn))
    }
}
