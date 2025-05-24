package no.iktdev.pfns.api.objects

abstract class RemoteDeviceBase(
    @Transient open val serverId: String?,
    @Transient open val receiverId: String
)