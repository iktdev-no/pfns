package no.iktdev.pfns

import java.io.File
import java.time.ZoneId
import java.util.Base64
import java.util.UUID

object Env {
    var isRuntimeOnlyApiKey: Boolean = false
        private set
    var isRuntimeOnlyWebKey: Boolean = false
        private set

    val firebaseServiceFile: File? = System.getenv("FirebaseServiceFile")?.let { File(it) }
    val targetApplicationPackageName: String? = System.getenv("AppPackageName")
    val apiTokenSecret: String = System.getenv("ApiTokenSecret") ?: run {
        isRuntimeOnlyApiKey = true
        runtimeGeneratedSecret()
    }
    val webTokenSecret: String = System.getenv("WebTokenSecret") ?: run {
        isRuntimeOnlyWebKey = true
        runtimeGeneratedSecret()
    }
    val timeZone: ZoneId = System.getenv("TZ")?.let { ZoneId.of(it) } ?: ZoneId.of("UTC")

    val googleAuthenticationClientId: String? = System.getenv("GoogleAuthenticationClientId")

    val databasePort: String? = System.getenv("DatabasePort")
    val databaseName: String? = System.getenv("DatabaseName")
    val databaseAddress: String? = System.getenv("DatabaseAddress")
    val databaseUsername: String? = System.getenv("DatabaseUsername")
    val databasePassword: String? = System.getenv("DatabasePassword")

    private fun runtimeGeneratedSecret(): String {
        val uuid = UUID.randomUUID().toString()
        return Base64.getEncoder().encodeToString(uuid.toByteArray())
    }
}