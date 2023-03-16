package dog.catfood.controllers

import dog.catfood.logic.DeviceService
import dog.catfood.logic.LocationService
import dog.catfood.models.Device
import dog.catfood.models.LimitCursor
import dog.catfood.models.Location
import dog.catfood.models.getUserPrincipal
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.POST
import dog.catfood.plugins.controllers.authentication.Authenticate
import dog.catfood.plugins.controllers.routesHelper
import dog.catfood.plugins.modelbinding.ValidBindResult
import dog.catfood.plugins.modelbinding.receiveModel
import dog.catfood.plugins.modelbinding.respondView
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respondRedirect
import jakarta.validation.constraints.NotBlank

@Authenticate("auth-session")
class DevicesController(
    private val deviceService: DeviceService,
    private val locationService: LocationService
): Controller {
    @GET("/dashboard/devices")
    suspend fun index(call: ApplicationCall) {
        call.respondRedirect(call.routesHelper.href("Dashboard", "index"))
    }

    @GET("/dashboard/devices/{deviceId}")
    suspend fun details(call: ApplicationCall, deviceId: Long) {
        val user = call.getUserPrincipal()
        val device = deviceService.getDevice(deviceId, user.id)
            ?: throw NotFoundException("Device $deviceId not found")
        val location = locationService.getLocations(device.id, user.id, LimitCursor(1)).firstOrNull()
        call.respondView(GetDeviceResponse(device, location))
    }

    @GET("/dashboard/devices/{deviceId}/live")
    suspend fun live(call: ApplicationCall, deviceId: Long) {
        val user = call.getUserPrincipal()
        val device = deviceService.getDevice(deviceId, user.id)
            ?: throw NotFoundException("Device $deviceId not found")
        val location = locationService.getLocations(device.id, user.id, LimitCursor(1)).firstOrNull()
        call.respondView(GetDeviceLiveResponse(device, location))
    }

    @GET("/dashboard/devices/new")
    suspend fun new(call: ApplicationCall) {
        call.respondView()
    }

    @POST("/dashboard/devices/new")
    suspend fun create(call: ApplicationCall) {
        val bindResult = call.receiveModel<CreateDeviceRequest>()
        if (bindResult is ValidBindResult) {
            val newDeviceRequest = bindResult.model
            val user = call.getUserPrincipal()
            val device = deviceService.createDevice(newDeviceRequest.name, user.id)
            return call.respondRedirect(
                call.routesHelper.href("Devices", "details") {
                    append("deviceId", device.id.toString())
                }
            )
        }
        call.respondView(template = "/device/new.ftl", bindResult)
    }
}

data class CreateDeviceRequest(
    @get:NotBlank
    val name: String
)

data class GetDeviceResponse(
    val device: Device,
    val location: Location?
)

data class GetDeviceLiveResponse(
    val device: Device,
    val location: Location?
)