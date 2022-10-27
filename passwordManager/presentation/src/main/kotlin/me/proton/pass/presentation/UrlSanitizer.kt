package me.proton.pass.presentation

import me.proton.pass.common.api.Result
import java.net.URI
import java.net.URISyntaxException

object UrlSanitizer {
    fun sanitize(url: String): Result<String> {
        if (url.isEmpty()) return Result.Success(url)

        // If it doesn't have a scheme, add https://
        val urlWithScheme = if (!url.contains("://")) {
            "https://$url"
        } else {
            url
        }

        try {
            val parsed = URI(urlWithScheme)
            val meaningfulSection = "${parsed.scheme}://${parsed.host}"
            return Result.Success(meaningfulSection)
        } catch (e: URISyntaxException) {
            return Result.Error(e)
        }
    }
}
