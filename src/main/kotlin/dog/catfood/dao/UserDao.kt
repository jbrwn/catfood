package dog.catfood.dao

import dog.catfood.jooq.Tables.USERS
import dog.catfood.models.User
import org.jooq.DSLContext
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine

class UserDao(private val context: DSLContext) {
    suspend fun getUserByUsername(username: String): User? {
        return context.transactionCoroutine { t ->
            t.dsl().select(
                USERS.ID,
                USERS.USERNAME,
                USERS.CREATED_ON,
                USERS.MODIFIED_ON
            )
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .awaitFirstOrNull()
                ?.map { it.toUser() }
        }
    }

    suspend fun createUser(username: String, passwordHash: String): User {
        return context.transactionCoroutine { t ->
            t.dsl().insertInto(USERS)
                .columns(USERS.USERNAME, USERS.PASSWORD)
                .values(username, passwordHash)
                .returning(USERS.ID, USERS.USERNAME)
                .awaitSingle()
                .map { it.toUser() }

        }
    }

    suspend fun getUserPasswordHash(id: Long): String? {
        return context.transactionCoroutine { t ->
            t.dsl().select(USERS.PASSWORD)
                .from(USERS)
                .where(USERS.ID.eq(id))
                .awaitFirstOrNull()
                ?.getValue(USERS.PASSWORD)
        }
    }
}

fun Record.toUser(): User =
    User(
        getValue(USERS.ID),
        getValue(USERS.USERNAME)
    )
