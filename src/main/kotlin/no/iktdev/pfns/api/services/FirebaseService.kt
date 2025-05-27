package no.iktdev.pfns.api.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.gson.Gson
import com.google.gson.JsonObject
import mu.KotlinLogging
import no.iktdev.pfns.Env
import org.springframework.stereotype.Service
import java.io.FileInputStream
import kotlin.system.exitProcess

@Service
class FirebaseService {
    val log = KotlinLogging.logger {}


    var firebaseApp: FirebaseApp? = null

    init {
        val firebaseServiceFile = Env.firebaseServiceFile

        if (firebaseServiceFile?.exists() != true) {
            log.error { "FirebaseService file not found at ${firebaseServiceFile?.absolutePath}. Please provide a valid service account JSON file." }
        } else {
            FileInputStream(firebaseServiceFile.absolutePath).use { fis ->
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(fis))
                    .build()
                firebaseApp = FirebaseApp.initializeApp(options)
            }
            if (firebaseApp != null) {
                FileInputStream(firebaseServiceFile.absolutePath).use { fis ->
                    val content = Gson().fromJson(fis.reader(), JsonObject::class.java)
                    val accountType = content.get("type").asString
                    val projectId = content.get("project_id").asString
                    log.info("Instantiated FirebaseApp on $projectId using a $accountType")
                }
            }

        }
    }

    fun isFirebaseAppInitialized(): Boolean {
        return firebaseApp != null
    }

    fun sendMessage(
        message: Message
    ): Boolean {
        if (!isFirebaseAppInitialized()) {
            log.error { "FirebaseApp is not initialized." }
            return false
        }
        return try {
            FirebaseMessaging.getInstance(firebaseApp).send(message)
            true
        } catch (e: Exception) {
            log.error(e) { "Failed to send message to Firebase: $message" }
            false
        }
    }

}