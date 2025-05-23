package no.iktdev.pfns.database

import org.jetbrains.exposed.sql.Database

abstract class DataSource(val databaseName: String, val address: String, val port: String?, val username: String, val password: String) {

    abstract fun createDatabase(): Database?

    abstract fun createDatabaseStatement(): String

    abstract fun toConnectionUrl(): String

    fun toPortedAddress(): String {
        return if (!address.contains(":") && port?.isBlank() != true) {
            "$address:$port"
        } else address
    }

    /**
     * Creates a database instance and connects to it
     */
    abstract fun toDatabase(): Database

}
