package proton.android.pass.totp.api

import proton.android.pass.common.api.Result

interface GetTotpCodeFromUri {
    suspend operator fun invoke(uri: String): Result<String>
}
