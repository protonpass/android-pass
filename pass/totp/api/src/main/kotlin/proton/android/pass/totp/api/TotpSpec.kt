package proton.android.pass.totp.api

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

enum class TotpAlgorithm {
    Sha1,
    Sha256,
    Sha512
}

enum class TotpDigits(val digits: Int) {
    Six(6),
    Eight(8)
}

data class TotpSpec(
    val secret: String,
    val label: String,
    val issuer: Option<String> = None,
    val algorithm: TotpAlgorithm = TotpAlgorithm.Sha1,
    val digits: TotpDigits = TotpDigits.Six,
    val validPeriodSeconds: Int = DEFAULT_VALID_PERIOD_SECONDS
) {
    companion object {
        const val DEFAULT_VALID_PERIOD_SECONDS = 30
    }
}
