package proton.android.pass.totp.impl

import proton.android.pass.common.api.Result
import proton.android.pass.common.api.toOption
import proton.android.pass.totp.api.MalformedOtpUri
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder

object OtpUriParser {

    private const val SCHEME = "otpauth"
    private const val HOST = "totp"

    private const val QUERY_SECRET = "secret"
    private const val QUERY_ISSUER = "issuer"
    private const val QUERY_ALGORITHM = "algorithm"
    private const val QUERY_DIGITS = "digits"
    private const val QUERY_PERIOD = "period"

    private const val SHA1_ALGORITHM = "SHA1"
    private const val SHA256_ALGORITHM = "SHA256"
    private const val SHA512_ALGORITHM = "SHA512"

    private const val DEFAULT_ALGORITHM = SHA1_ALGORITHM
    private const val DEFAULT_DIGITS = "6"
    private const val DEFAULT_PERIOD = "30"

    fun parse(input: String): Result<TotpSpec> =
        try {
            val parsed = URI(input)
            extractFields(parsed)
        } catch (e: URISyntaxException) {
            Result.Error(MalformedOtpUri.InvalidUri(e))
        }

    @Suppress("ComplexMethod", "ReturnCount")
    private fun extractFields(parsed: URI): Result<TotpSpec> {
        val scheme = parsed.scheme
        if (scheme == null || scheme.isEmpty()) return Result.Error(MalformedOtpUri.MissingScheme)
        if (scheme != SCHEME) return Result.Error(MalformedOtpUri.InvalidScheme(parsed.scheme))

        val host = parsed.host
        if (host == null || host.isEmpty()) return Result.Error(MalformedOtpUri.MissingHost)
        if (host != HOST) return Result.Error(MalformedOtpUri.InvalidHost(parsed.host))

        if (parsed.path.isEmpty()) return Result.Error(MalformedOtpUri.MissingLabel)
        val label = parsed.path.removePrefix("/")

        val parsedQuery = splitQuery(parsed.query)

        val secretList =
            parsedQuery[QUERY_SECRET] ?: return Result.Error(MalformedOtpUri.MissingSecret)
        val secret = secretList.firstOrNull() ?: return Result.Error(MalformedOtpUri.MissingSecret)

        val issuer = parsedQuery[QUERY_ISSUER]?.firstOrNull().toOption()

        val algorithmString = parsedQuery[QUERY_ALGORITHM]?.firstOrNull() ?: DEFAULT_ALGORITHM
        val algorithm = when (algorithmString) {
            SHA1_ALGORITHM -> TotpAlgorithm.Sha1
            SHA256_ALGORITHM -> TotpAlgorithm.Sha256
            SHA512_ALGORITHM -> TotpAlgorithm.Sha512
            else -> return Result.Error(MalformedOtpUri.InvalidAlgorithm(algorithmString))
        }

        val digitsString = parsedQuery[QUERY_DIGITS]?.firstOrNull() ?: DEFAULT_DIGITS
        val digits = when (digitsString) {
            "6" -> TotpDigits.Six
            "8" -> TotpDigits.Eight
            else -> return Result.Error(MalformedOtpUri.InvalidDigitCount(digitsString))
        }

        val periodString = parsedQuery[QUERY_PERIOD]?.firstOrNull() ?: DEFAULT_PERIOD
        val period = periodString.toIntOrNull() ?: return Result.Error(
            MalformedOtpUri.InvalidValidity(periodString)
        )

        val spec = TotpSpec(
            secret = secret,
            label = label,
            issuer = issuer,
            algorithm = algorithm,
            digits = digits,
            validPeriodSeconds = period
        )
        return Result.Success(spec)
    }

    private fun splitQuery(query: String): Map<String, List<String?>> {
        val queryPairs: MutableMap<String, MutableList<String?>> = mutableMapOf()
        val pairs = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            val key = if (idx > 0) URLDecoder.decode(
                pair.substring(0, idx),
                Charsets.UTF_8.name()
            ) else pair
            if (!queryPairs.containsKey(key)) {
                queryPairs[key] = mutableListOf()
            }
            val value = if (idx > 0 && pair.length > idx + 1) URLDecoder.decode(
                pair.substring(idx + 1),
                Charsets.UTF_8.name()
            ) else null
            queryPairs[key]!!.add(value)
        }
        return queryPairs
    }
}
