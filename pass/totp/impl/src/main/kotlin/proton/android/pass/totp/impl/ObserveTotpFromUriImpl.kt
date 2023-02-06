package proton.android.pass.totp.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class ObserveTotpFromUriImpl @Inject constructor(
    private val totpManager: TotpManager
) : ObserveTotpFromUri {

    override fun invoke(uri: String): Result<Flow<ObserveTotpFromUri.TotpWrapper>> =
        totpManager.parse(uri)
            .mapCatching { spec ->
                totpManager.observeCode(spec)
                    .map { ObserveTotpFromUri.TotpWrapper(it.first, it.second) }
            }
}
