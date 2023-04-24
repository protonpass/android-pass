package proton.android.pass.totp.api

import java.net.URISyntaxException

sealed class MalformedOtpUri : IllegalArgumentException() {
    data class InvalidUri(val throwable: URISyntaxException) : MalformedOtpUri()
    data class InvalidScheme(val scheme: String) : MalformedOtpUri()
    data class InvalidHost(val host: String) : MalformedOtpUri()
    object MissingScheme : MalformedOtpUri()
    object MissingHost : MalformedOtpUri()
    object MissingSecret : MalformedOtpUri()
    data class InvalidAlgorithm(val algorithm: String) : MalformedOtpUri()
    data class InvalidDigitCount(val digits: String) : MalformedOtpUri()
    data class InvalidValidity(val validity: String) : MalformedOtpUri()
}


