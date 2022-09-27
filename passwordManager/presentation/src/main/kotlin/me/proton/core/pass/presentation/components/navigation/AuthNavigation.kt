package me.proton.core.pass.presentation.components.navigation

import androidx.compose.runtime.Stable
import me.proton.core.domain.entity.UserId

@Stable
data class AuthNavigation(
    val onSignIn: (UserId?) -> Unit,
    val onSignOut: (UserId) -> Unit,
    val onRemove: (UserId?) -> Unit,
    val onSwitch: (UserId) -> Unit
)
