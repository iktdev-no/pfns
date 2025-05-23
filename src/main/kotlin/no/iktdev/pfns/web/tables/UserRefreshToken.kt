package no.iktdev.pfns.web.tables

import no.iktdev.pfns.Env
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

object UserRefreshToken: IntIdTable() {
    val jit = varchar("jit", 256).uniqueIndex()
    val email = varchar("email", 254).uniqueIndex()
    val token = varchar("token", 256).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
    val lastUsedAt = datetime("last_used_at").nullable()
    val userAgent = varchar("user_agent", 512).nullable()
    val ipAddress = varchar("ip_address", 64).nullable()
    val revoked = bool("revoked").default(false)



    fun storeRefreshToken(email: String, token: String, expiresAt: Instant): String {
        return transaction {
            // Sjekk om brukeren allerede har et refresh-token
            val existingRowId = UserRefreshToken.selectAll()
                .where { UserRefreshToken.email eq email }
                .map { it -> it[UserRefreshToken.id].value }
                .firstOrNull()
            val jit = UUID.randomUUID().toString() + "-refresh"
            if (existingRowId != null) {
                // Oppdater eksisterende rad og returner ID
                UserRefreshToken.update ({ UserRefreshToken.email eq email }) {
                    it[UserRefreshToken.jit] = jit
                    it[UserRefreshToken.token] = token
                    it[UserRefreshToken.expiresAt] = LocalDateTime.ofInstant(expiresAt, Env.timeZone)
                    it[UserRefreshToken.createdAt] = LocalDateTime.now()
                    it[UserRefreshToken.revoked] = false
                }
            } else {
                // Sett inn ny rad og returner ID
                UserRefreshToken.insertAndGetId {
                    it[UserRefreshToken.jit] = jit
                    it[UserRefreshToken.email] = email
                    it[UserRefreshToken.token] = token
                    it[UserRefreshToken.expiresAt] = LocalDateTime.ofInstant(expiresAt, Env.timeZone)
                    it[UserRefreshToken.createdAt] = LocalDateTime.now()
                }
            }
            jit
        }
    }


    fun refreshTokenExistsAndIsValid(refreshToken: String): Boolean {
        return transaction {
            UserRefreshToken.selectAll()
                .where { token eq refreshToken }
                .andWhere { revoked eq false }
                .count() != 0L
        }
    }

    fun isRefreshTokenValid(jit: String): Boolean {
        return transaction {
            UserRefreshToken.selectAll()
                .where { UserRefreshToken.jit eq jit }
                .andWhere { revoked eq false }
                .singleOrNull()?.let { !it[revoked] } ?: false
        }
    }

    fun getEmailFromRefreshToken(refreshToken: String): String? {
        return transaction {
            UserRefreshToken.select(email)
                .where { token eq refreshToken }
                .map { it -> it[email] }
                .singleOrNull()
        }
    }
}