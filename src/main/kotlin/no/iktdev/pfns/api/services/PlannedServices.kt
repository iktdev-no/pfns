package no.iktdev.pfns.api.services

import no.iktdev.pfns.api.table.ApiToken
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@EnableScheduling
@Service
class PlannedServices {

    @Scheduled(cron = "0 0 0 * * ?") // Kjør ved midnatt
    fun resetUsage() {
        try {
            transaction {
                ApiToken.update({ ApiToken.usage neq 0 }) {
                    it[usage] = 0
                }
            }
            println("Usage har blitt tilbakestilt for tokens")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}