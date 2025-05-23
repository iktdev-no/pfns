package no.iktdev.pfns

import java.io.File
import java.time.ZoneId

object Env {
    val firebaseServiceFile: File? = System.getenv("FirebaseServiceFile")?.let { File(it) }
    val targetApplicationPackageName: String? = System.getenv("AppPackageName")
    val apiTokenSecret: String = System.getenv("ApiTokenSecret")
    val webTokenSecret: String = System.getenv("WebTokenSecret")
    val timeZone: ZoneId = System.getenv("TZ")?.let { ZoneId.of(it) } ?: ZoneId.of("UTC")

    val googleAuthenticationClientId: String? = System.getenv("GoogleAuthenticationClientId")

    val databasePort: String? = System.getenv("DatabasePort")
    val databaseName: String? = System.getenv("DatabaseName")
    val databaseAddress: String? = System.getenv("DatabaseAddress")
    val databaseUsername: String? = System.getenv("DatabaseUsername")
    val databasePassword: String? = System.getenv("DatabasePassword")
}