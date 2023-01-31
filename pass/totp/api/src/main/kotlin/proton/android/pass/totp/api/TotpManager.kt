package proton.android.pass.totp.api

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Result

interface TotpManager {
    fun generateUri(spec: TotpSpec): String
    fun observeCode(spec: TotpSpec): Flow<Pair<String, Int>>
    fun parse(uri: String): Result<TotpSpec>
}
