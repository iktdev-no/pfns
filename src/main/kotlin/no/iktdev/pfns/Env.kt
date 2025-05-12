package no.iktdev.pfns

import java.io.File

object Env {
    val firebaseServiceFile: File? = System.getenv("FirebaseServiceFile")?.let { File(it) }
}