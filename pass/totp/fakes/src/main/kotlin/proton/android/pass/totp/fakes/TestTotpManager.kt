package proton.android.pass.totp.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import proton.android.pass.common.api.Result
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import javax.inject.Inject

class TestTotpManager @Inject constructor() : TotpManager {

    override fun generateUri(spec: TotpSpec): String = ""

    override fun observeCode(spec: TotpSpec): Flow<Pair<String, Int>> = emptyFlow()

    override fun parse(uri: String): Result<TotpSpec> = Result.Loading
}
