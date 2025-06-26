package no.iktdev.pfns.api.controllers

import no.iktdev.pfns.api.table.DeviceIdentifiers
import no.iktdev.pfns.api.table.RegisteredDevices
import no.iktdev.pfns.getRequestersIp
import no.iktdev.pfns.interceptor.Mode
import no.iktdev.pfns.interceptor.RequiresApiAuthentication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/fcm/register")
class RegisterDeviceController {

    @RequiresApiAuthentication(Mode.None)
    @PostMapping("receiver")
    fun registerClient(@RequestBody fcmReceiverId: String, request: HttpServletRequest): ResponseEntity<String> {
        val receiverFcmId = fcmReceiverId.trim('"')
        if (receiverFcmId.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("FCM Receiver ID cannot be blank.")
        }
        val existingPfnsId = DeviceIdentifiers.getPfnsId(receiverFcmId)
        val pfnsReceiverId = existingPfnsId ?: generatePfnsId()
        val success = if (existingPfnsId.isNullOrBlank()) {
             DeviceIdentifiers.storeIdentifier(receiverFcmId, pfnsReceiverId)
        } else true

        val existingRegister = RegisteredDevices.findRegisteredDevice(pfnsReceiverId).any { it.serverId == null }

        val registerSuccess = if (!existingRegister) {
            RegisteredDevices.storeApiTokenOrUpdate(
                serverId = null,
                pfnsReceiverId = pfnsReceiverId,
                ip = request.getRequestersIp()
            )
        } else {
            true
        }
        return if (success && registerSuccess) {
            ResponseEntity.ok(pfnsReceiverId)
        } else {
            ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Failed to register device.")
        }
    }

    fun generatePfnsId(): String = UUID.randomUUID().toString().replace("-", "")
}