package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.runtime.Stable

@Stable
sealed interface IsTotpUriCreatedState {
    object Unknown : IsTotpUriCreatedState
    data class Success(val totpUri: String) : IsTotpUriCreatedState
}
