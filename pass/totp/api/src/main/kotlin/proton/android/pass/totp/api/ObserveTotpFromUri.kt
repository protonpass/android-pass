package proton.android.pass.totp.api

import kotlinx.coroutines.flow.Flow

interface ObserveTotpFromUri {
    operator fun invoke(uri: String): Result<Flow<TotpManager.TotpWrapper>>
}
