package dog.catfood.controllers

import dog.catfood.logic.UserService
import dog.catfood.models.AppSession
import dog.catfood.models.isLoggedIn
import dog.catfood.models.login
import dog.catfood.models.toUserPrinciple
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.POST
import dog.catfood.plugins.controllers.routesHelper
import dog.catfood.plugins.modelbinding.ValidBindResult
import dog.catfood.plugins.modelbinding.receiveModel
import dog.catfood.plugins.modelbinding.respondView
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class AccountController(
    private val userService: UserService
): Controller {
    @GET("/account/new")
    suspend fun new(call: ApplicationCall, next: String?) {
        if (call.isLoggedIn()) {
            return call.respondRedirect(call.routesHelper.href("Dashboard", "index"))
        }
        call.respondView(viewData = mapOf("next" to next))
    }

    @POST("/account/new")
    suspend fun create(call: ApplicationCall, next: String?) {
        val bindResult = call.receiveModel<CreateAccountRequest>()
        if (bindResult is ValidBindResult) {
            val model = bindResult.model
            if (userService.getUserByUsername(model.username) != null) {
                bindResult.errors.addParamError("username", "username ${model.username} is already taken")
            } else {
                val user = userService.createUser(model.username, model.password)
                call.login(user.toUserPrinciple())
                return call.respondRedirect(
                    next ?: call.routesHelper.href("Dashboard", "index")
                )
            }
        }
        call.respondView(template = "/account/new.ftl", bindResult = bindResult, viewData = mapOf("next" to next))
    }

    @GET("/account/login")
    suspend fun login(call: ApplicationCall, next: String?) {
        if (call.isLoggedIn()) {
            return call.respondRedirect(
                call.routesHelper.href("Dashboard", "index")
            )
        }
        call.respondView(viewData = mapOf("next" to next))
    }

    @POST("/account/login")
    suspend fun doLogin(call: ApplicationCall, next: String?) {
        val bindResult = call.receiveModel<LoginRequest>()
        if (bindResult is ValidBindResult) {
            val model = bindResult.model
            val user = userService.getUserByUsernameAndPassword(model.username, model.password)
            if (user != null) {
                call.login(user.toUserPrinciple())
                return call.respondRedirect(
                    next ?: call.routesHelper.href("Dashboard", "index")
                )
            } else {
                bindResult.errors.addGlobalError("Invalid username or password")
            }
        }
        call.respondView(template = "/account/login.ftl", bindResult = bindResult, viewData = mapOf("next" to next))
    }

    @GET("/account/logout")
    suspend fun logout(call: ApplicationCall) {
        call.respondView()
    }

    @POST("/account/logout")
    suspend fun doLogout(call: ApplicationCall) {
        call.sessions.clear<AppSession>()
        call.respondRedirect(call.routesHelper.href("Home", "index"))
    }
}

data class CreateAccountRequest(
    @get:NotBlank
    val username: String,

    @get:NotBlank
    @get:Size(min=6, message="must be at least {min} characters")
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)
