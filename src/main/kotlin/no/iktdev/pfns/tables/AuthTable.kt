package no.iktdev.no.iktdev.pfns.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object AccessTokens : IntIdTable("accessToken") {
    val serverId = varchar("serverId", 64)
    val token = text("token")
    val expiry = datetime("expiry")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val revoked = bool("revoked").default(false)
}

object RefreshTokens : IntIdTable("refreshToken") {
    val accessTokenId = reference("accessTokenId", AccessTokens)
    val token = text("token")
    val expiry = datetime("expiry")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
}
