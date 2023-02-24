package proton.android.pass.totp.api

import kotlinx.coroutines.flow.Flow

interface TotpManager {
    fun generateUri(spec: TotpSpec): String
    fun generateUriWithDefaults(secret: String): String
    fun observeCode(spec: TotpSpec): Flow<TotpWrapper>
    fun parse(uri: String): Result<TotpSpec>

    data class TotpWrapper(
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    )
}
