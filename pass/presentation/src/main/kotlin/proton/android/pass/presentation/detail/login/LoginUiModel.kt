package proton.android.pass.presentation.detail.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

@Stable
data class LoginUiModel(
    val title: String,
    val username: String,
    val password: PasswordState,
    val websites: ImmutableList<String>,
    val note: String
)
