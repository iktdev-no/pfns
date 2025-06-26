package no.iktdev.pfns.api.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object DeviceIdentifiers : IntIdTable("DeviceIdentifiers") {
    val pfnsId = varchar("pfnsId", 32).uniqueIndex() // Short, public identifier
    val fcmReceiverId = varchar("fcmReceiverId", 256) // Internal FCM token

    init {
        uniqueIndex(pfnsId, fcmReceiverId)
    }

    fun getPfnsId(fcmId: String): String? {
        return transaction {
            DeviceIdentifiers.select(pfnsId)
                .where { DeviceIdentifiers.fcmReceiverId eq fcmId }
                .map { it -> it[DeviceIdentifiers.pfnsId] }
                .singleOrNull()
        }
    }

    fun getFcmReceiverId(pfnsReceiverId: String): String? {
        return transaction {
            DeviceIdentifiers.select(pfnsId)
                .where { DeviceIdentifiers.pfnsId eq pfnsReceiverId }
                .map { it -> it[DeviceIdentifiers.fcmReceiverId] }
                .singleOrNull()
        }
    }

    fun storeIdentifier(fcmReceiverId: String, pfnsReceiverId: String): Boolean {
        return try {
            transaction {
                val updatedRows = DeviceIdentifiers.update({ DeviceIdentifiers.pfnsId eq pfnsReceiverId }) {
                    it[DeviceIdentifiers.fcmReceiverId] = fcmReceiverId
                }

                if (updatedRows == 0) { // Hvis ingen rader ble oppdatert, gjør en insert
                    DeviceIdentifiers.insert {
                        it[DeviceIdentifiers.pfnsId] = pfnsReceiverId
                        it[DeviceIdentifiers.fcmReceiverId] = fcmReceiverId
                    }
                }
            }
            true
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}