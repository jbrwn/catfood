package dog.catfood.controllers

import dog.catfood.logic.DeviceService
import dog.catfood.logic.LocationService
import dog.catfood.models.getDevicePrincipal
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.POST
import dog.catfood.plugins.controllers.authentication.Authenticate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.time.OffsetDateTime

@Authenticate("auth-cert")
class DeviceApiController(
    private val deviceService: DeviceService,
    private val locationService: LocationService,
): Controller {
    @GET("/device")
    suspend fun getDevice(call: ApplicationCall) {
        val devicePrincipal = call.getDevicePrincipal()
        val device = deviceService.getDevice(devicePrincipal.id)
            ?: throw NotFoundException("Device ${devicePrincipal.id} not found")
        call.respond(device)
    }

    @POST("/device/location")
    suspend fun createLocation(call: ApplicationCall) {
        val devicePrincipal = call.getDevicePrincipal()
        val request = call.receive<CreateLocationRequest>()
        val location = locationService.createLocation(devicePrincipal.id, request)
        call.respond(location)
    }
}

data class CreateLocationRequest(
    val longitude: Double, // decimal degrees
    val latitude: Double, // decimal degrees
    val altitude: Double, // meters
    val speed: Double, // knots
    val angle: Double, // Degrees from true north
    val magneticVariation: Double, // Degrees from true north
    val timestamp: OffsetDateTime, // fix time
)
