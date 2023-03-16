package dog.catfood.models

data class User(
    val id: Long,
    val username: String
)

fun User.toUserPrinciple(): UserPrincipal {
    return UserPrincipal(id, username)
}