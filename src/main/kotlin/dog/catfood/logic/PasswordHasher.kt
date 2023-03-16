package dog.catfood.logic

interface PasswordHasher {
    fun hashPassword(password: String): String
    fun checkPassword(password: String, hash: String): Boolean
}