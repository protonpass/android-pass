package proton.android.pass.totp.api

import proton.android.pass.common.api.LoadingResult

interface GetTotpCodeFromUri {
    suspend operator fun invoke(uri: String): LoadingResult<String>
}
