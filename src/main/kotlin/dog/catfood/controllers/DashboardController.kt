package dog.catfood.controllers

import dog.catfood.logic.DeviceService
import dog.catfood.models.Device
import dog.catfood.models.getUserPrincipal
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.authentication.Authenticate
import dog.catfood.plugins.modelbinding.respondView
import io.ktor.server.application.ApplicationCall

@Authenticate("auth-session")
class DashboardController(
    private val deviceService: DeviceService
): Controller {
    @GET("/dashboard")
    suspend fun index(call: ApplicationCall) {
        val user = call.getUserPrincipal()
        val devices = deviceService.getDevices(user.id)
        call.respondView(template = "/dashboard/index.ftl", model = DashboardModel(devices))
    }
}

data class DashboardModel(
    val devices: List<Device>
)
