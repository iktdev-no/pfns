package no.iktdev.pfns.web.userAccess

data class AuthenticatedUser(
    val name: String,
    val email: String,
    val oAuthUserId: String
)