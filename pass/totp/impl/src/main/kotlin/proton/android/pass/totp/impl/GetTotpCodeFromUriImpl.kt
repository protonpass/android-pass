package proton.android.pass.totp.impl

import kotlinx.coroutines.flow.first
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class GetTotpCodeFromUriImpl @Inject constructor(
    private val totpManager: TotpManager
) : GetTotpCodeFromUri {
    override suspend fun invoke(uri: String): LoadingResult<String> =
        totpManager.parse(uri)
            .map { totpManager.observeCode(it).first().first }
}
