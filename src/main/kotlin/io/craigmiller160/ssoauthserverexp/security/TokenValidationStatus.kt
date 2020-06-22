package io.craigmiller160.ssoauthserverexp.security

enum class TokenValidationStatus {
    VALID,
    BAD_SIGNATURE,
    EXPIRED,
    NO_TOKEN,
    RESOURCE_FORBIDDEN,
    VALIDATION_ERROR
}