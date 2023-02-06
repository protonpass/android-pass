package proton.android.pass.totp.fakes

import proton.android.pass.totp.api.GetTotpCodeFromUri
import javax.inject.Inject

class TestGetTotpCodeFromUri @Inject constructor() : GetTotpCodeFromUri {
    override suspend fun invoke(uri: String): Result<String> = Result.success("")
}
