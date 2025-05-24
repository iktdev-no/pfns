package no.iktdev.pfns.api.table

import no.iktdev.pfns.api.controllers.RegisterDeviceController
import no.iktdev.pfns.api.objects.RegisterDeviceObject
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object RegisteredDevices : IntIdTable("RegisteredDevice") {
    val serverId = varchar("serverId", 64).nullable() // Having this nullable removes restriction
    val receiverId = text("receiverId")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val lastUsed = datetime("lastUsed").nullable()
    val ip = varchar("ip", 45)

    init {
        uniqueIndex(serverId, receiverId)
    }

    fun storeApiTokenOrUpdate(serverId: String?, receiverId: String, ip: String?): Boolean {
        return try {
            transaction {
                val updatedRows = RegisteredDevices.update({ RegisteredDevices.receiverId eq receiverId }) {
                    it[RegisteredDevices.serverId] = serverId
                    it[RegisteredDevices.ip] = ip ?: "unknown"
                }

                if (updatedRows == 0) { // Hvis ingen rader ble oppdatert, gjør en insert
                    RegisteredDevices.insert {
                        it[RegisteredDevices.serverId] = serverId
                        it[RegisteredDevices.receiverId] = receiverId
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
                (RegisteredDevices.receiverId eq receiverId) and
                        (RegisteredDevices.serverId eq serverId or (RegisteredDevices.serverId.isNull()))
            }.map { it -> RegisterDeviceObject(
                serverId = it[RegisteredDevices.serverId],
                receiverId = it[RegisteredDevices.receiverId]
            ) }
        }
    }



    fun updateLastUsed(serverId: String, receiverId: String)  {
        transaction {
            RegisteredDevices.update({
                (RegisteredDevices.receiverId eq receiverId) and
                        (RegisteredDevices.serverId eq serverId)
            }) {
                it[RegisteredDevices.lastUsed] = CurrentDateTime
            }
        }
    }




}