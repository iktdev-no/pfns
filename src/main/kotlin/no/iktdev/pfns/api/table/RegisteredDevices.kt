package no.iktdev.pfns.api.table

import no.iktdev.pfns.api.objects.RegisterDeviceObject
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.text.get

object RegisteredDevices : IntIdTable("RegisteredDevice") {
    val serverId = varchar("serverId", 64).nullable() // Having this nullable removes restriction
    val pfnsReceiverId = varchar("pfnsReceiverId", 256) // This is the ID used by PFNS to identify the device
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val lastUsed = datetime("lastUsed").nullable()
    val ip = varchar("ip", 45)

    init {
        uniqueIndex(serverId, pfnsReceiverId)
    }

    fun exists(serverId: String?, pfnsReceiverId: String): Boolean {
        return transaction {
            RegisteredDevices.selectAll()
                .where { (RegisteredDevices.serverId eq serverId) and (RegisteredDevices.pfnsReceiverId eq pfnsReceiverId) }
                .empty().not()
        }
    }

    fun storeApiTokenOrUpdate(serverId: String?, pfnsReceiverId: String, ip: String?): Boolean {
        return try {
            transaction {
                val updatedRows = RegisteredDevices.update({ RegisteredDevices.pfnsReceiverId eq pfnsReceiverId }) {
                    it[RegisteredDevices.serverId] = serverId
                    it[RegisteredDevices.ip] = ip ?: "unknown"
                }

                if (updatedRows == 0) { // Hvis ingen rader ble oppdatert, gjør en insert
                    RegisteredDevices.insert {
                        it[RegisteredDevices.serverId] = serverId
                        it[RegisteredDevices.pfnsReceiverId] = pfnsReceiverId
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

    fun findRegisteredDevice(pfnsReceiverId: String): List<RegisterDeviceObject> {
        return transaction {
            RegisteredDevices
                .innerJoin(DeviceIdentifiers, { RegisteredDevices.pfnsReceiverId}, { DeviceIdentifiers.pfnsId })
                .selectAll()
                .where { RegisteredDevices.pfnsReceiverId eq pfnsReceiverId }
                .map {
                    RegisterDeviceObject(
                        serverId = it[RegisteredDevices.serverId],
                        fcmReceiverId = it[DeviceIdentifiers.fcmReceiverId]
                    )
                }
        }
    }



    fun updateLastUsed(serverId: String, pfnsReceiverId: String)  {
        transaction {
            RegisteredDevices.update({
                (RegisteredDevices.pfnsReceiverId eq pfnsReceiverId) and
                        (RegisteredDevices.serverId eq serverId)
            }) {
                it[RegisteredDevices.lastUsed] = CurrentDateTime
            }
        }
    }




}