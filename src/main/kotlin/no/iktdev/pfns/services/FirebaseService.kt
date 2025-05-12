package no.iktdev.pfns.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import mu.KotlinLogging
import no.iktdev.pfns.Env
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.stereotype.Service
import java.io.FileInputStream
import kotlin.system.exitProcess

//@Service
class FirebaseService {
    val log = KotlinLogging.logger {}


    var firebaseApp: FirebaseApp? = null

    init {
        if (Env.firebaseServiceFile?.exists() != true) {
            log.error { "No firebase service file found!" }
       //     exitProcess(1)
        } else {
            FileInputStream(Env.firebaseServiceFile.absolutePath).use { fis ->
                val content = JSONObject(String(fis.readBytes()))
                val accountType = content.getString("type")
                val projectId = content.getString("project_id")
                fis.reset()
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(fis))
                    .build()
                firebaseApp = FirebaseApp.initializeApp(options)
                log.info("Instantiated FirebaseApp on $projectId using a $accountType")
            }
        }

    }
}