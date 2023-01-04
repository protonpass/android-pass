package me.proton.android.pass.composecomponents.impl.messages

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.proton.android.pass.notifications.api.SnackbarType
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

@Composable
fun rememberPassSnackbarHostState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): PassSnackbarHostState = remember {
    PassSnackbarHostState(ProtonSnackbarHostState(snackbarHostState))
}

@Stable
class PassSnackbarHostState(
    val protonSnackbarHostState: ProtonSnackbarHostState = ProtonSnackbarHostState()
) {
    suspend fun showSnackbar(
        type: SnackbarType,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) = protonSnackbarHostState.showSnackbar(
        type = when (type) {
            SnackbarType.SUCCESS -> ProtonSnackbarType.SUCCESS
            SnackbarType.WARNING -> ProtonSnackbarType.WARNING
            SnackbarType.ERROR -> ProtonSnackbarType.ERROR
            SnackbarType.NORM -> ProtonSnackbarType.NORM
        },
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}
