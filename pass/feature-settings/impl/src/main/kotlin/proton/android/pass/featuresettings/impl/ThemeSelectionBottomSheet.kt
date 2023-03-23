package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ThemeSelectionBottomSheet(
    dismissBottomSheet: () -> Unit,
    viewModel: ThemeSelectorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ThemeSelectionBottomSheetContents(themePreference = state) {
        viewModel.onThemePreferenceChange(it)
        dismissBottomSheet()
    }
}
