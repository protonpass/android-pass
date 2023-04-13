package proton.android.pass.featuresettings.impl.primaryvault

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectPrimaryVaultBottomSheet(
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit,
    onClose: () -> Unit,
    viewModel: SelectPrimaryVaultViewModel = hiltViewModel()
) {
    BackHandler { onClose() }

    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.event == SelectPrimaryVaultEvent.Selected) {
        LaunchedEffect(Unit) { onSuccess() }
    }

    SelectPrimaryVaultContents(
        modifier = modifier.bottomSheet(),
        vaults = state.vaults,
        loading = state.loading.value(),
        onVaultSelected = { viewModel.onVaultSelected(it) }
    )
}
