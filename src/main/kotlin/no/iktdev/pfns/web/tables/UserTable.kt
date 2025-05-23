package no.iktdev.pfns.web.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserTable: IntIdTable("Users") {
    val userId = uuid("userId").autoGenerate().uniqueIndex()
    val oAuthUserId = text("uid")
    val name = varchar("name", 64)
    val email = varchar("email", 254)
    val disabled = bool("disabled").default(false)

    fun createUser(oAuthUserId: String, name: String, email: String) {
        transaction {
            UserTable.insert {
                it[UserTable.oAuthUserId] = oAuthUserId
                it[UserTable.name] = name
                it[UserTable.email] = email
            }
        }
    }

    fun doesUserExist(email: String): Boolean {
        val user = getUser(email)
        return user != null
    }


    fun getUser(email: String): User? {
        return transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .map { row ->
                    User(
                        userId = row[userId].toString(),
                        name = row[name],
                        email = row[UserTable.email],
                        disabled = row[disabled]
                    )
                }
                .firstOrNull()
        }
    }
}


data class User(
    val userId: String,
    val name: String,
    val email: String,
    val disabled: Boolean
)