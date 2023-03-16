package dog.catfood.models

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Principal
import io.ktor.server.auth.principal

open class ClaimsPrincipal(
    val id: Long
): Principal

class UserPrincipal(
    id: Long,
    val username: String
): ClaimsPrincipal(id)

class DevicePrincipal(
    id: Long,
): ClaimsPrincipal(id)

fun ApplicationCall.getUserPrincipal() = principal<UserPrincipal>() ?: throw ClaimsPrincipalNotFoundException()

fun ApplicationCall.getDevicePrincipal() = principal<DevicePrincipal>() ?: throw ClaimsPrincipalNotFoundException()

class ClaimsPrincipalNotFoundException : Exception()