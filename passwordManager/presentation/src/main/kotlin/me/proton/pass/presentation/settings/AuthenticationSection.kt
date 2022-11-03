package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.settings.SettingPreferenceDescription
import me.proton.pass.presentation.components.settings.SettingPreferenceTitle
import me.proton.pass.presentation.components.settings.SettingSectionTitle
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.value

@Composable
fun AuthenticationSection(
    modifier: Modifier = Modifier,
    isToggleChecked: IsButtonEnabled,
    onToggleChange: (IsButtonEnabled) -> Unit
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SettingSectionTitle(text = stringResource(R.string.settings_authentication_section_title))
        Row {
            SettingPreferenceTitle(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .weight(1f),
                text = stringResource(R.string.settings_authentication_preference_title)
            )
            Switch(
                checked = isToggleChecked.value(),
                onCheckedChange = { onToggleChange(IsButtonEnabled.from(it)) }
            )
        }

        SettingPreferenceDescription(
            text = stringResource(R.string.settings_authentication_preference_description)
        )
    }
}
