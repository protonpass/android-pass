package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.core.compose.component.ProtonSettingsList
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    appVersion: String,
    onOpenThemeSelection: () -> Unit,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit,
    onToggleAutofillChange: (Boolean) -> Unit,
    onForceSyncClick: () -> Unit
) {
    ProtonSettingsList(modifier = modifier) {
        if (state.autofillStatus is AutofillSupportedStatus.Supported) {
            item {
                AutofillSection(
                    state = state.autofillStatus.status.value(),
                    onToggleChange = onToggleAutofillChange
                )
                Divider(modifier = Modifier.fillMaxWidth())
            }
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

        item {
            AppSection(
                appVersion = appVersion,
                onForceSyncClick = onForceSyncClick
            )
        }
    }
}
