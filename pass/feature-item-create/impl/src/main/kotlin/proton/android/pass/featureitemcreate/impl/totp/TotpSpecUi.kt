package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

@Stable
data class TotpSpecUi(
    val secret: String,
    val label: String,
    val issuer: String,
    val algorithm: TotpAlgorithm = TotpAlgorithm.Sha1,
    val digits: TotpDigits = TotpDigits.Six,
    val validPeriodSeconds: Int? = TotpSpec.DEFAULT_VALID_PERIOD_SECONDS
) {
    fun validate(): Set<TotpSpecValidationErrors> {
        val mutableSet = mutableSetOf<TotpSpecValidationErrors>()
        if (secret.isBlank()) mutableSet.add(TotpSpecValidationErrors.BlankSecret)
        if (validPeriodSeconds == null) mutableSet.add(TotpSpecValidationErrors.BlankValidTime)
        return mutableSet.toSet()
    }

    fun toTotSpec(): TotpSpec = TotpSpec(
        secret = this.secret,
        label = this.label,
        issuer = if (this.issuer.isBlank()) None else this.issuer.toOption(),
        algorithm = this.algorithm,
        digits = this.digits,
        validPeriodSeconds = this.validPeriodSeconds ?: 0
    )
}

sealed interface TotpSpecValidationErrors {
    object BlankSecret : TotpSpecValidationErrors
    object BlankLabel : TotpSpecValidationErrors
    object BlankValidTime : TotpSpecValidationErrors
}
