package dog.catfood.logic

import org.mindrot.jbcrypt.BCrypt

class BcryptPasswordHasher: PasswordHasher {
    override fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    override fun checkPassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}