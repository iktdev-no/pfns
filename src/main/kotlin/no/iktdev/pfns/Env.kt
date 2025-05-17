package no.iktdev.pfns

import java.io.File
import java.time.ZoneId

object Env {
    val firebaseServiceFile: File? = System.getenv("FirebaseServiceFile")?.let { File(it) }
    val targetApplicationPackageName: String? = System.getenv("AppPackageName")
    val jwtSecret: String = System.getenv("JwtSecret")
    val timeZone = System.getenv("TZ")?.let { ZoneId.of(it) } ?: ZoneId.of("UTC")
}