package proton.android.pass.data.api.url

import proton.android.pass.common.api.Option

sealed interface HostInfo {
    data class Host(
        val subdomain: Option<String>,
        val domain: String,
        val tld: Option<String>
    ) : HostInfo

    data class Ip(val ip: String) : HostInfo
}

interface HostParser {
    fun parse(url: String): Result<HostInfo>
}
