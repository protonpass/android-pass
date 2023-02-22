package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.commonuimodels.api.PackageInfoUi

@Stable
data class LoginDetailUiState(
    val title: String,
    val username: String,
    val password: PasswordState,
    val websites: ImmutableList<String>,
    val packageInfoSet: ImmutableSet<PackageInfoUi>,
    val note: String,
    val totpUiState: TotpUiState?,
    val isLoading: Boolean,
    val isItemSentToTrash: Boolean
)

@Stable
data class TotpUiState(
    val code: String,
    val remainingSeconds: Int,
    val totalSeconds: Int
)
