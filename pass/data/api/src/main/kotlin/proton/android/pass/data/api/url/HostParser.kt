package proton.android.pass.data.api.url

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result

sealed interface HostInfo {
    data class Host(
        val subdomain: Option<String>,
        val domain: String,
        val tld: Option<String>
    ) : HostInfo

    class Ip : HostInfo
}

interface HostParser {
    fun parse(url: String): Result<HostInfo>
}
