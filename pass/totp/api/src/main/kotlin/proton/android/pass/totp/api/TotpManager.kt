package proton.android.pass.totp.api

import proton.android.pass.common.api.Result

interface TotpManager {
    fun generateUri(spec: TotpSpec): String
    fun calculateCode(spec: TotpSpec): String
    fun parse(uri: String): Result<TotpSpec>
}
