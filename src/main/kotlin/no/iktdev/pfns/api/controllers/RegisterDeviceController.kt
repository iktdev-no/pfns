package no.iktdev.pfns.api.controllers

import no.iktdev.pfns.api.objects.RegisterDeviceObject
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
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/fcm/register")
class RegisterDeviceController {

    @RequiresApiAuthentication(Mode.None)
    @PostMapping("receiver")
    fun registerClient(@RequestBody data: RegisterDeviceObject, request: HttpServletRequest): ResponseEntity<Boolean> {
        if (data.fcmReceiverId.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false)
        }
        val success = RegisteredDevices.storeApiTokenOrUpdate(
            serverId = data.serverId,
            fcmReceiverId = data.fcmReceiverId,
            ip = request.getRequestersIp()
        )
        return if (success) {
            ResponseEntity.ok(true)
        } else {
            ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false)
        }
    }
}