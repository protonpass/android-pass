package proton.android.pass.totp.api

interface GetTotpCodeFromUri {
    suspend operator fun invoke(uri: String): Result<String>
}
