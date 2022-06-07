package io.craigmiller160.authserver.dto.tokenResponse

interface TokenValues {
  val accessToken: String
  val refreshToken: String
  val tokenId: String
}
