package proton.android.pass.totp.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveTotpFromUri @Inject constructor() : ObserveTotpFromUri {

    private val flow = FlowUtils.testFlow<Result<TotpManager.TotpWrapper>>()

    fun sendValue(value: Result<TotpManager.TotpWrapper>) {
        flow.tryEmit(value)
    }

    override fun invoke(uri: String): Flow<TotpManager.TotpWrapper> = flow.map { it.getOrThrow() }
}
