package no.iktdev.pfns.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object AccessToken : IntIdTable("accessToken") {
    val serverId = varchar("serverId", 64)
    val token = text("token")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val ip = varchar("ip", 45)
    val revoked = bool("revoked").default(false)
}

data class AccessTokenObject(
    val rowId: Int,
    val serverId: String,
    val token: String,
    val createdAt: LocalDateTime,
    val ip: String,
    val revoked: Boolean
) {
    companion object {
        fun fromRow(row: ResultRow) = AccessTokenObject(
            rowId = row[AccessToken.id].value,
            serverId = row[AccessToken.serverId],
            token = row[AccessToken.token],
            createdAt = row[AccessToken.createdAt],
            ip = row[AccessToken.ip],
            revoked = row[AccessToken.revoked]
        )
    }
}

fun findValidAccessToken(serverId: String): AccessTokenObject? {
    return transaction {
        AccessToken.selectAll()
            .where { AccessToken.serverId eq serverId }
            .map { AccessTokenObject.fromRow(it) }
            .singleOrNull()
    }
}