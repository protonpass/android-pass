package proton.android.pass.totp.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class ObserveTotpFromUriImpl @Inject constructor(
    private val totpManager: TotpManager
) : ObserveTotpFromUri {

    override fun invoke(uri: String): Flow<TotpManager.TotpWrapper> =
        totpManager.parse(uri)
            .fold(
                onSuccess = { totpManager.observeCode(it) },
                onFailure = {
                    flow { throw it }
                }
            )
}

