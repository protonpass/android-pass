package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClearClipboardOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (SettingsNavigation) -> Unit,
    viewModel: ClearClipboardOptionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.isClearClipboardOptionSaved == IsClearClipboardOptionSaved.Success) {
        LaunchedEffect(Unit) { onNavigate(SettingsNavigation.DismissBottomSheet) }
    }
    ClearClipboardOptionsBottomSheetContents(
        modifier = modifier,
        clearClipboardPreference = state.clearClipboardPreference,
        onClearClipboardSettingSelected = viewModel::onClearClipboardSettingSelected,
    )
}
