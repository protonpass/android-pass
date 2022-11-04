package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.ButtonEnabledPreviewProvider
import me.proton.pass.presentation.components.settings.SettingPreferenceDescription
import me.proton.pass.presentation.components.settings.SettingPreferenceTitle
import me.proton.pass.presentation.components.settings.SettingSectionTitle
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.value

@Composable
fun AuthenticationSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isToggleChecked: IsButtonEnabled,
    onToggleChange: (IsButtonEnabled) -> Unit
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        SettingSectionTitle(text = stringResource(R.string.settings_authentication_section_title))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingPreferenceTitle(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .weight(1f),
                text = stringResource(R.string.settings_authentication_preference_title)
            )
            Switch(
                enabled = enabled,
                checked = isToggleChecked.value(),
                onCheckedChange = { onToggleChange(IsButtonEnabled.from(it)) }
            )
        }

        val description = if (enabled) {
            R.string.settings_authentication_preference_description_enabled
        } else {
            R.string.settings_authentication_preference_description_no_fingerprint
        }
        SettingPreferenceDescription(
            text = stringResource(description)
        )
    }
}

class ThemedBooleanPreviewProvider :
    ThemePairPreviewProvider<IsButtonEnabled>(ButtonEnabledPreviewProvider())

@Preview
@Composable
fun AuthenticationSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, IsButtonEnabled>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AuthenticationSection(
                isToggleChecked = input.second,
                onToggleChange = {},
                enabled = true
            )
        }
    }
}
