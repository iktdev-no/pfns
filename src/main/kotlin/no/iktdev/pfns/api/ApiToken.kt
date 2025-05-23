package no.iktdev.pfns.api

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object ApiToken : IntIdTable("apiToken") {
    val serverId = varchar("serverId", 64)
    val email = varchar("email", 254)
    val token = text("token")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val ip = varchar("ip", 45)
    val revoked = bool("revoked").default(false)
    val usage = integer("usage").default(0)

    init {
        uniqueIndex(serverId, email)
    }

    fun findValidApiToken(serverId: String): ApiTokenObject? {
        return transaction {
            ApiToken.selectAll()
                .where { ApiToken.serverId eq serverId }
                .map { ApiTokenObject.fromRow(it) }
                .singleOrNull()
        }
    }

    fun getMyTokens(email: String): List<ApiTokenObject> {
        return transaction {
            ApiToken.selectAll()
                .where { ApiToken.email eq email }
                .map { ApiTokenObject.fromRow(it) }
        }
    }

    fun storeApiToken(serverId: String, email: String, ip: String, token: String): String? {
        return try {
            transaction {
                ApiToken.insert {
                    it[ApiToken.serverId] = serverId
                    it[ApiToken.email] = email
                    it[ApiToken.token] = token
                    it[ApiToken.ip] = ip
                }
                commit()
            }
            token
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteApiToken(serverId: String, email: String): Boolean {
        return try {
            transaction {
                val deleted = ApiToken.deleteWhere(1) {
                    (ApiToken.serverId eq serverId) and (ApiToken.email eq email)
                }
                val success = deleted > 0
                if (success)
                    commit()
                else
                    rollback()
                success
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}


data class ExposableApiTokenObject(
    val serverId: String,
    val email: String,
    val createdAt: LocalDateTime,
    val ip: String,
    val usage: Int,
    val revoked: Boolean
)

data class ApiTokenObject(
    val serverId: String,
    val email: String,
    val token: String,
    val createdAt: LocalDateTime,
    val ip: String,
    val usage: Int,
    val revoked: Boolean
) {
    companion object {
        fun fromRow(row: ResultRow) = ApiTokenObject(
            email = row[ApiToken.email],
            serverId = row[ApiToken.serverId],
            token = row[ApiToken.token],
            createdAt = row[ApiToken.createdAt],
            ip = row[ApiToken.ip],
            revoked = row[ApiToken.revoked],
            usage = row[ApiToken.usage]
        )
    }

    fun toExposable(): ExposableApiTokenObject {
        return ExposableApiTokenObject(
            serverId = this.serverId,
            email = this.email,
            createdAt = this.createdAt,
            ip = this.ip,
            usage = this.usage,
            revoked = this.revoked
        )
    }
}

