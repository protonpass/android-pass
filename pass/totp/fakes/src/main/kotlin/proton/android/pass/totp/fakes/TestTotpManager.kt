package proton.android.pass.totp.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestTotpManager @Inject constructor() : TotpManager {

    private var parseResult: Result<TotpSpec> = Result.failure(NotImplementedError())

    fun setParseResult(result: Result<TotpSpec>) {
        parseResult = result
    }

    override fun generateUri(spec: TotpSpec): String = ""

    override fun generateUriWithDefaults(secret: String): String = ""

    override fun observeCode(spec: TotpSpec): Flow<TotpManager.TotpWrapper> = emptyFlow()

    override fun parse(uri: String): Result<TotpSpec> = parseResult
}
