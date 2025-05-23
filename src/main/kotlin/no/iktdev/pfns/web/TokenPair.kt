package no.iktdev.pfns.web

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
