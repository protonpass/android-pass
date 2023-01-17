package proton.android.pass.totp.api

interface TotpCalculator {
    fun calculate(spec: TotpSpec): String
}
