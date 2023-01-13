package proton.android.pass.data.api.url

import proton.android.pass.common.api.Result
import java.net.URI
import java.net.URISyntaxException

object UrlSanitizer {
    fun sanitize(url: String): Result<String> {
        if (url.isEmpty()) return Result.Error(IllegalArgumentException("url cannot be empty"))

        // If it doesn't have a scheme, add https://
        val urlWithScheme = if (!url.contains("://")) {
            "https://$url"
        } else {
            url
        }

        return try {
            val parsed = URI(urlWithScheme)
            val meaningfulSection = "${parsed.scheme}://${parsed.host}${parsed.path}"
            Result.Success(meaningfulSection)
        } catch (e: URISyntaxException) {
            Result.Error(e)
        }
    }

    fun getDomain(url: String): Result<String> =
        when (val res = sanitize(url)) {
            Result.Loading -> Result.Loading
            is Result.Error -> res
            is Result.Success -> {
                try {
                    val parsed = URI(res.data)
                    Result.Success(parsed.host)
                } catch (e: URISyntaxException) {
                    Result.Error(e)
                }
            }
        }

}
