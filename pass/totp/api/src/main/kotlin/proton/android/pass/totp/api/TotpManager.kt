package proton.android.pass.totp.api

interface TotpManager {
    fun generateUri(spec: TotpSpec): String
    fun calculateCode(spec: TotpSpec): String
}
