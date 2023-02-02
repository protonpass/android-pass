package proton.android.pass.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.ProtonSettingsList
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.preferences.value

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onOpenThemeSelection: () -> Unit,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit,
    onToggleAutofillChange: (Boolean) -> Unit,
    onCopyToClipboardChange: (Boolean) -> Unit,
    onForceSyncClick: () -> Unit,
    onAppVersionClick: (String) -> Unit
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
        item {
            CopyTotpToClipboardSection(
                state = state.copyTotpToClipboard.value(),
                onToggleChange = onCopyToClipboardChange
            )
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

        item {
            AppSection(
                appVersion = state.appVersion,
                onForceSyncClick = onForceSyncClick,
                onAppVersionClick = onAppVersionClick
            )
        }
    }
}
