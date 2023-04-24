package proton.android.pass.totp.impl

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.flatMap
import proton.android.pass.log.api.PassLogger
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class ObserveTotpFromUriImpl @Inject constructor(
    private val totpManager: TotpManager
) : ObserveTotpFromUri {

    override fun invoke(uri: String): Result<Flow<TotpManager.TotpWrapper>> =
        totpManager.parse(uri)
            .onFailure {
                PassLogger.d(TAG, it, "Failed to parse TOTP uri")
                PassLogger.w(TAG, "Failed to parse TOTP uri")
            }
            .flatMap { spec ->
                runCatching {
                    totpManager.observeCode(spec)
                }.onFailure { PassLogger.w(TAG, it, "Failed to observe TOTP code") }
            }


    companion object {
        private const val TAG = "ObserveTotpFromUriImpl"
    }
}
