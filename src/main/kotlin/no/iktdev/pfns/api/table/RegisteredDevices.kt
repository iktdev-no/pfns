package no.iktdev.pfns.api.table

import no.iktdev.pfns.api.objects.RegisterDeviceObject
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object RegisteredDevices : IntIdTable("RegisteredDevice") {
    val serverId = varchar("serverId", 64).nullable() // Having this nullable removes restriction
    //val pfnsReceiverId = varchar("pfnsReceiverId", 256) // This is the ID used by PFNS to identify the device
    val fcmReceiverId = varchar("fcmReceiverId", 256)
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val lastUsed = datetime("lastUsed").nullable()
    val ip = varchar("ip", 45)

    init {
        uniqueIndex(serverId, fcmReceiverId)
    }

    fun storeApiTokenOrUpdate(serverId: String?, fcmReceiverId: String, ip: String?): Boolean {
        return try {
            transaction {
                val updatedRows = RegisteredDevices.update({ RegisteredDevices.fcmReceiverId eq fcmReceiverId }) {
                    it[RegisteredDevices.serverId] = serverId
                    it[RegisteredDevices.ip] = ip ?: "unknown"
                }

                if (updatedRows == 0) { // Hvis ingen rader ble oppdatert, gjør en insert
                    RegisteredDevices.insert {
                        it[RegisteredDevices.serverId] = serverId
                        it[RegisteredDevices.fcmReceiverId] = fcmReceiverId
                        it[RegisteredDevices.ip] = ip ?: "unknown"
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

    fun findRegisteredDevice(serverId: String?, receiverId: String): List<RegisterDeviceObject> {
        return transaction {
            RegisteredDevices
                .selectAll().where {
                (RegisteredDevices.fcmReceiverId eq receiverId) and
                        (RegisteredDevices.serverId eq serverId or (RegisteredDevices.serverId.isNull()))
            }.map { it -> RegisterDeviceObject(
                serverId = it[RegisteredDevices.serverId],
                fcmReceiverId = it[RegisteredDevices.fcmReceiverId]
            ) }
        }
    }



    fun updateLastUsed(serverId: String, receiverId: String)  {
        transaction {
            RegisteredDevices.update({
                (RegisteredDevices.fcmReceiverId eq receiverId) and
                        (RegisteredDevices.serverId eq serverId)
            }) {
                it[RegisteredDevices.lastUsed] = CurrentDateTime
            }
        }
    }




}