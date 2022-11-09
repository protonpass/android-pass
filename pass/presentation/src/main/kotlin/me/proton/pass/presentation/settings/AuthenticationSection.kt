package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.ButtonEnabledPreviewProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.value

@Composable
fun AuthenticationSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isToggleChecked: IsButtonEnabled,
    onToggleChange: (IsButtonEnabled) -> Unit
) {
    val description = if (enabled) {
        R.string.settings_authentication_preference_description_enabled
    } else {
        R.string.settings_authentication_preference_description_no_fingerprint
    }

    val value = if (enabled) {
        isToggleChecked.value()
    } else {
        null
    }

    Column(modifier = modifier) {
        ProtonSettingsHeader(title = R.string.settings_authentication_section_title)
        ProtonSettingsToggleItem(
            name = stringResource(R.string.settings_authentication_preference_title),
            value = value,
            onToggle = { onToggleChange(IsButtonEnabled.from(it)) },
            hint = stringResource(description)
        )
    }
}

class ThemedButtonEnabledPreviewProvider :
    ThemePairPreviewProvider<IsButtonEnabled>(ButtonEnabledPreviewProvider())

@Preview
@Composable
fun AuthenticationSectionPreview(
    @PreviewParameter(ThemedButtonEnabledPreviewProvider::class) input: Pair<Boolean, IsButtonEnabled>
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
