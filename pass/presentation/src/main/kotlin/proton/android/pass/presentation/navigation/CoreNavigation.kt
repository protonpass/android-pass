package proton.android.pass.presentation.navigation

import androidx.compose.runtime.Stable
import me.proton.core.domain.entity.UserId

@Stable
data class CoreNavigation(
    val onSignIn: (UserId?) -> Unit,
    val onSignOut: (UserId) -> Unit,
    val onRemove: (UserId?) -> Unit,
    val onSwitch: (UserId) -> Unit,
    val onReport: () -> Unit
)
