package no.iktdev.pfns.api.objects

data class RegisterDeviceObject(
    val serverId: String?,
    val fcmReceiverId: String
)