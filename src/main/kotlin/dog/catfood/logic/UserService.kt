package dog.catfood.logic

import dog.catfood.dao.UserDao
import dog.catfood.models.User

class UserService(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher
) {
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByUsernameAndPassword(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username) ?: return null
        val hash = userDao.getUserPasswordHash(user.id) ?: return null
        return if (passwordHasher.checkPassword(password, hash)) {
            user
        } else {
            null
        }
    }

    suspend fun createUser(username: String, password: String): User {
        val hash = passwordHasher.hashPassword(password)
        return userDao.createUser(username, hash)
    }
}
