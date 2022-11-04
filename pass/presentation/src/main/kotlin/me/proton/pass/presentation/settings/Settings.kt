package me.proton.pass.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onOpenThemeSelection: () -> Unit,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit
) {
    Column(modifier = modifier) {
        AutofillSection(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        Divider(modifier = Modifier.fillMaxWidth())

        if (state.fingerprintSection != FingerprintSectionState.NotAvailable) {
            val (enabled, toggleChecked) = when (val res = state.fingerprintSection) {
                FingerprintSectionState.NoFingerprintRegistered -> false to IsButtonEnabled.Disabled
                is FingerprintSectionState.Available -> true to res.enabled
                else -> false to IsButtonEnabled.Disabled
            }
            AuthenticationSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = enabled,
                isToggleChecked = toggleChecked,
                onToggleChange = onFingerPrintLockChange
            )
            Divider(modifier = Modifier.fillMaxWidth())
        }

        AppearanceSection(
            modifier = Modifier
                .clickable(onClick = onOpenThemeSelection)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            theme = state.themePreference
        )
    }
}
