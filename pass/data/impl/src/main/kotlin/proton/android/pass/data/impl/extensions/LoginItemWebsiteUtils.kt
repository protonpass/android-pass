package proton.android.pass.data.impl.extensions

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.pass.domain.ItemType
import java.net.URL

fun ItemType.Login.hasWebsite(website: String): Boolean {
    val websiteUrl = when (val parsed = parseUrl(website)) {
        None -> return false
        is Some -> parsed.value
    }

    for (w in websites) {
        val parsed = when (val parsed = parseUrl(w)) {
            None -> continue
            is Some -> parsed.value
        }

        // If one of the two URLs has a port defined, we don't consider it as match
        if (parsed.port != websiteUrl.port && parsed.port != -1) continue

        // If the port has matched and the hosts match, it's a match
        if (parsed.host == websiteUrl.host) return true
    }
    return false
}

private fun parseUrl(url: String): Option<URL> {
    val parsed = runCatching {
        URL(url)
    }.getOrNull() ?: runCatching {
        URL("https://$url")
    }.getOrNull()

    return parsed.toOption()
}
