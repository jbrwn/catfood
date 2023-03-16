package dog.catfood.models

import dog.catfood.plugins.csrf.csrf
import dog.catfood.plugins.flash.TempData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

class AppSession(
    val userPrincipal: UserPrincipal,
    override val tempData: MutableMap<String, String> = mutableMapOf()
): TempData

fun ApplicationCall.isLoggedIn() = sessions.get<AppSession>() != null

fun ApplicationCall.login(userPrincipal: UserPrincipal) {
    sessions.set(AppSession(userPrincipal))
    csrf.rotateCookie()
}
