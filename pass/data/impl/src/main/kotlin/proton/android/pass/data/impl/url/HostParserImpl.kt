package proton.android.pass.data.impl.url

import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.GetPublicSuffixList
import javax.inject.Inject

class HostParserImpl @Inject constructor(
    private val getPublicSuffixList: GetPublicSuffixList
) : HostParser {

    override fun parse(url: String): Result<HostInfo> = UrlSanitizer.getDomain(url).fold(
        onSuccess = { getHostInfoFromDomain(it) },
        onFailure = { Result.failure(it) }
    )

    private fun getHostInfoFromDomain(domain: String): Result<HostInfo> =
        if (isIp(domain)) {
            Result.success(HostInfo.Ip(domain))
        } else {
            parseHostInfo(domain)
        }

    private fun parseHostInfo(domain: String): Result<HostInfo.Host> {
        val publicSuffixes = getPublicSuffixList()
        val parts = domain.split('.')

        if (parts.size == 1) {
            return handleDomainWithSinglePart(domain, publicSuffixes)
        }

        // Has multiple parts, find the widest match that is a TLD
        for (i in parts.indices) {
            val portion = stringFromParts(parts, i)
            if (publicSuffixes.contains(portion)) {
                // We found the TLD
                return Result.success(hostWithTld(parts, i, portion))
            }
        }

        // We did not find a TLD
        return Result.success(hostWithoutTld(parts))
    }

    private fun handleDomainWithSinglePart(
        domain: String,
        publicSuffixes: Set<String>
    ): Result<HostInfo.Host> =
        if (publicSuffixes.contains(domain)) {
            Result.failure(IllegalArgumentException("host is a TLD"))
        } else {
            Result.success(
                HostInfo.Host(
                    subdomain = None,
                    domain = domain,
                    tld = None
                )
            )
        }

    private fun hostWithTld(parts: List<String>, tldStartingPart: Int, tld: String): HostInfo.Host {
        val domain = parts[tldStartingPart - 1]
        val subdomain = if (tldStartingPart == 1) {
            // It means that we have no subdomain, as the part 0 is the domain
            None
        } else {
            buildString {
                var portions = 0
                for (i in 0 until tldStartingPart - 1) {
                    if (portions > 0) append('.')
                    append(parts[i])
                    portions++
                }
            }.some()
        }


        return HostInfo.Host(
            subdomain = subdomain,
            domain = domain,
            tld = tld.some()
        )
    }

    private fun hostWithoutTld(parts: List<String>): HostInfo.Host {
        // We did not find a TLD, so we'll just assume that:
        // - The last portion is the tld
        // - The second to last is the domain
        // - The rest is the subdomain

        val tld = parts.last()

        val hostInfo = if (parts.size > 2) {
            val subdomain = buildString {
                var portions = 0
                for (i in 0 until parts.size - 2) {
                    if (portions > 0) append('.')
                    append(parts[i])
                    portions++
                }
            }


            HostInfo.Host(
                subdomain = subdomain.some(),
                domain = parts[parts.size - 2],
                tld = tld.some()
            )
        } else {
            // It's of the form a.b, so domain=a, tld=b and no subdomain
            HostInfo.Host(
                subdomain = None,
                domain = parts[0],
                tld = tld.some()
            )
        }

        return hostInfo
    }

    private fun stringFromParts(parts: List<String>, startingFrom: Int): String = buildString {
        var portions = 0
        for (i in startingFrom until parts.size) {
            if (portions > 0) append('.')
            append(parts[i])
            portions++
        }
    }


    private fun isIp(domain: String): Boolean = ipRegex.matches(domain)

    companion object {
        private val ipRegex = Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$")
    }
}
