package dog.catfood.controllers

import dog.catfood.logic.LocationService
import dog.catfood.models.Cursor
import dog.catfood.models.getUserPrincipal
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.ParamName
import dog.catfood.plugins.controllers.authentication.Authenticate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

@Authenticate("auth-session")
class LocationsApiController(
    private val locationService : LocationService
): Controller {
    @GET("/devices/{deviceId}/Locations")
    suspend fun index(
        call: ApplicationCall,
        deviceId: Long,
        limit: Int? = null,
        @ParamName("cursor_id") cursorId: Long? = null
    ) {
        val user = call.getUserPrincipal()
        val cursor = Cursor(cursorId, limit)
        val locations = locationService.getLocations(deviceId, user.id, cursor)
        call.respond(locations)
    }
}
