package io.craigmiller160.authserver.security

object GrantType {
    const val CLIENT_CREDENTIALS = "client_credentials"
    const val PASSWORD = "password"
    const val REFRESH_TOKEN = "refresh_token"
    const val AUTH_CODE = "authorization_code"

    fun isGrantTypeSupported(grantType: String): Boolean {
        return PASSWORD == grantType || REFRESH_TOKEN == grantType || AUTH_CODE == grantType;
    }

}
