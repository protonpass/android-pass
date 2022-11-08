package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.ProtonSettingsList
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onOpenThemeSelection: () -> Unit,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit
) {
    ProtonSettingsList(modifier = modifier) {
        item {
            AutofillSection()
            Divider(modifier = Modifier.fillMaxWidth())
        }

        if (state.fingerprintSection != FingerprintSectionState.NotAvailable) {
            val (enabled, toggleChecked) = when (val res = state.fingerprintSection) {
                FingerprintSectionState.NoFingerprintRegistered -> false to IsButtonEnabled.Disabled
                is FingerprintSectionState.Available -> true to res.enabled
                else -> false to IsButtonEnabled.Disabled
            }
            item {
                AuthenticationSection(
                    enabled = enabled,
                    isToggleChecked = toggleChecked,
                    onToggleChange = onFingerPrintLockChange
                )
                Divider(modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            AppearanceSection(
                theme = state.themePreference,
                onSelectThemeClick = onOpenThemeSelection
            )
            Divider(modifier = Modifier.fillMaxWidth())
        }

        item { AppSection() }
    }
}
