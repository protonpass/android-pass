package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.settings.SettingPreferenceDescription
import me.proton.pass.presentation.components.settings.SettingPreferenceTitle
import me.proton.pass.presentation.components.settings.SettingSectionTitle

@Composable
fun AutofillSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingSectionTitle(text = stringResource(R.string.settings_autofill_section_title))
        SettingPreferenceTitle(
            modifier = Modifier.padding(vertical = 20.dp),
            text = stringResource(R.string.settings_autofill_preference_title)
        )
        SettingPreferenceDescription(
            text = stringResource(R.string.settings_autofill_preference_description)
        )
    }
}

@Preview
@Composable
fun AutofillSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AutofillSection()
        }
    }
}
