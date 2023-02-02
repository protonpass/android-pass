package proton.android.pass.data.api.url

import proton.android.pass.common.api.LoadingResult
import java.net.URI
import java.net.URISyntaxException

object UrlSanitizer {
    fun sanitize(url: String): LoadingResult<String> {
        if (url.isEmpty()) return LoadingResult.Error(IllegalArgumentException("url cannot be empty"))

        // If it doesn't have a scheme, add https://
        val urlWithScheme = if (!url.contains("://")) {
            "https://$url"
        } else {
            url
        }

        return try {
            val parsed = URI(urlWithScheme)
            val meaningfulSection = "${parsed.scheme}://${parsed.host}${parsed.path}"
            LoadingResult.Success(meaningfulSection)
        } catch (e: URISyntaxException) {
            LoadingResult.Error(e)
        }
    }

    fun getDomain(url: String): LoadingResult<String> =
        when (val res = sanitize(url)) {
            LoadingResult.Loading -> LoadingResult.Loading
            is LoadingResult.Error -> res
            is LoadingResult.Success -> {
                try {
                    val parsed = URI(res.data)
                    LoadingResult.Success(parsed.host)
                } catch (e: URISyntaxException) {
                    LoadingResult.Error(e)
                }
            }
        }

}
