package no.iktdev.pfns.api.objects

import java.io.Serializable

data class RemoteServerObject(
    override val serverId: String?,
    override val pfnsReceiverId: String,
    val server: RemoteServer
) : RemoteDeviceBase(serverId, pfnsReceiverId) {
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