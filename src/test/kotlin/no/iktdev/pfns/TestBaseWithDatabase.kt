package no.iktdev.pfns

import com.fasterxml.jackson.databind.ObjectMapper
import no.iktdev.pfns.database.H2Datasource
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.collections.forEach

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestBaseWithDatabase: TestBase() {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setProps() {
            System.setProperty("ApiTokenSecret", "Potetmos")
            System.setProperty("WebTokenSecret", "Potetmos")
        }
    }

    lateinit var database: Database

    val mapper = ObjectMapper()
    val ds = H2Datasource("test")

    @BeforeAll
    fun setupDatabase() {
        database = ds.toDatabase2()
        System.out.println(ds)
        databaseSetup(database)
    }

    @BeforeAll
    fun verifyDatabaseSetup() {
        transaction {
            tables.forEach { table ->
                assertThat(table.exists()).isTrue
            }
        }
    }

    @AfterAll
    fun clearDatabase() {
        transaction {
            tables.forEach { table ->
                SchemaUtils.drop(table)
            }
        }
    }
}