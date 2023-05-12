package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurevault.impl.VaultNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectVaultBottomsheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: SelectVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SelectVaultBottomsheetContent(
        modifier = modifier,
        shareList = state.vaults,
        selectedShareId = state.selected.value()?.vault?.shareId,
        onVaultClick = {
            onNavigate(VaultNavigation.VaultSelected(it))
        }
    )

}
