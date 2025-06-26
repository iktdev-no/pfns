package no.iktdev.pfns.api.controllers

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.gson.Gson
import com.mysql.cj.Messages
import mu.KotlinLogging
import no.iktdev.pfns.api.AppAction
import no.iktdev.pfns.api.objects.RemoteServerObject
import no.iktdev.pfns.api.services.FirebaseService
import no.iktdev.pfns.api.table.RegisteredDevices
import no.iktdev.pfns.interceptor.Mode
import no.iktdev.pfns.interceptor.RequiresApiAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/fcm/config")
class PushConfigurationController(
    @Autowired val firebaseService: FirebaseService
) {
    val log = KotlinLogging.logger {}

    @RequiresApiAuthentication(Mode.Strict)
    @PostMapping("server")
    fun sendServerConfiguration(@RequestBody data: RemoteServerObject): ResponseEntity<String> {

        val registeredToServer = RegisteredDevices.findRegisteredDevice(data.pfnsReceiverId)
        if (registeredToServer.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not find a receiverId that is allowed to be used by you")
        }

        val fcmReceiverId = registeredToServer.find { it.serverId == data.serverId || it.serverId == null }?.fcmReceiverId
        if (fcmReceiverId.isNullOrBlank()) {
            log.error { "No FCM Receiver ID found for serverId: ${data.serverId} and pfnsReceiverId: ${data.pfnsReceiverId}" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No FCM Receiver ID found for the provided serverId and pfnsReceiverId")
        }


        val message = Message.builder()
            .putData("action", AppAction.ConfigureServer)
            .putData("server", Gson().toJson(data.server))
            .setToken(fcmReceiverId)
            .build()


        if (firebaseService.firebaseApp == null) {
            log.error { "Service/FirebaseApp is null" }
            return ResponseEntity.status(HttpStatus.METHOD_FAILURE).build()
        }
        val success = firebaseService.sendMessage(message)
        if (success) {
            log.info { "Sending requested payload on 'configure-server' over FCM using pfnsReceiverId: ${data.pfnsReceiverId}" }

        } else {
            log.error { "Failed to send message over FCM for pfnsReceiverId: ${data.pfnsReceiverId}" }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send message to FCM")
        }
        return ResponseEntity.ok().build()
    }
}