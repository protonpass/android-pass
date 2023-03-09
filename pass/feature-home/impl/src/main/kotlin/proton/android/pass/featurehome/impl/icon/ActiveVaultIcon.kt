package proton.android.pass.featurehome.impl.icon

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.extension.toBackgroundColor
import proton.android.pass.composecomponents.impl.extension.toIconColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ActiveVaultIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    onClick: () -> Unit = {},
    viewModel: ActiveVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    VaultIcon(
        modifier = modifier.padding(start = 8.dp),
        backgroundColor = state.properties.shareColor.toBackgroundColor(),
        iconColor = state.properties.shareColor.toIconColor(),
        size = size,
        icon = state.properties.shareIcon.toResource(),
        onClick = onClick
    )
}
