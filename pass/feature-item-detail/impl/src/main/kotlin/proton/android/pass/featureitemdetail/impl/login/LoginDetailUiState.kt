package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

@Stable
data class LoginDetailUiState(
    val title: String,
    val username: String,
    val password: PasswordState,
    val websites: ImmutableList<String>,
    val packageNames: ImmutableList<String>,
    val note: String,
    val totpUiState: TotpUiState?
)

@Stable
data class TotpUiState(
    val code: String,
    val remainingSeconds: Int
)
