package io.craigmiller160.authserver.dto.tokenResponse

data class TokenCookieResponse(
    override val accessToken: String,
    override val refreshToken: String,
    override val tokenId: String,
    val accessTokenCookie: String,
    val refreshTokenCookie: String,
    val redirectUri: String?
) : TokenValues {
  val tokenResponse = TokenResponse(accessToken, refreshToken, tokenId)
}
