package me.proton.pass.presentation

import me.proton.pass.common.api.Result
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
            val meaningfulSection = "${parsed.scheme}://${parsed.host}"
            Result.Success(meaningfulSection)
        } catch (e: URISyntaxException) {
            Result.Error(e)
        }
    }
}
