package me.proton.core.pass.presentation.components.common

import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType

@Composable
fun PassSnackbarHost(modifier: Modifier = Modifier, snackbarHostState: PassSnackbarHostState) {
    ProtonSnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState.protonSnackbarHostState
    )
}

@Stable
class PassSnackbarHostState(
    val protonSnackbarHostState: ProtonSnackbarHostState = ProtonSnackbarHostState()
) {
    suspend fun showSnackbar(
        type: ProtonSnackbarType,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) = protonSnackbarHostState.showSnackbar(
        type = type,
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}
