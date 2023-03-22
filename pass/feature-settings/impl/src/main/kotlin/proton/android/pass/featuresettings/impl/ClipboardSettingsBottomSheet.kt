package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClipboardBottomSheet(
    modifier: Modifier = Modifier,
    onClearClipboardSettingClick: () -> Unit,
    viewModel: ClipboardSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ClipboardBottomSheetContents(
        modifier = modifier,
        state = state,
        onClearClipboardSettingClick = onClearClipboardSettingClick,
        onCopyTotpSettingClick = viewModel::onCopyToClipboardChange
    )
}
