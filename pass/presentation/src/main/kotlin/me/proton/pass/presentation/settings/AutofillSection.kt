package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@Composable
fun AutofillSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ProtonSettingsHeader(title = R.string.settings_autofill_section_title)
        ProtonSettingsItem(
            name = stringResource(R.string.settings_autofill_preference_title),
            hint = stringResource(R.string.settings_autofill_preference_description)
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
