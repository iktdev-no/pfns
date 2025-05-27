package no.iktdev.pfns.api.objects

import java.io.Serializable

data class RemoteServerObject(
    override val serverId: String?,
    override val receiverId: String,
    val server: RemoteServer
) : RemoteDeviceBase(serverId, receiverId) {
}


data class RemoteServer(
    val id: String,
    var name: String,
    var fingerprint: String?,
    val lan: String,
    val remote: String? = null,
    var remoteSecure: Boolean = false
) :
    Serializable