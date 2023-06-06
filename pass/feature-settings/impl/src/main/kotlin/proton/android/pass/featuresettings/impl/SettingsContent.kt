package proton.android.pass.featuresettings.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.loading.LoadingDialog
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.preferences.value

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.title_settings),
                onUpClick = { onEvent(SettingsContentEvent.Up) }
            )
        }
    ) { contentPadding ->
        if (state.isLoadingState == IsLoadingState.Loading) {
            LoadingDialog()
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(PassTheme.colors.backgroundStrong)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferencesSection(
                theme = state.themePreference,
                onEvent = onEvent,
            )
            PrivacySection(
                useFavicons = state.useFavicons.value(),
                allowScreenshots = state.allowScreenshots.value(),
                onEvent = onEvent
            )
            AnimatedVisibility(visible = state.showPrimaryVaultSelector) {
                PrimaryVaultSection(
                    primaryVault = state.primaryVault,
                    onEvent = onEvent
                )
            }
            AboutSection(
                onEvent = onEvent
            )
            ApplicationSection(
                shareTelemetry = state.shareTelemetry,
                shareCrashes = state.shareCrashes,
                onEvent = onEvent
            )
        }
    }
}
