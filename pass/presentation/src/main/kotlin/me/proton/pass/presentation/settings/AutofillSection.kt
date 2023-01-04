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
import me.proton.pass.commonui.api.BooleanPreviewProvider

@Composable
fun AutofillSection(
    modifier: Modifier = Modifier,
    state: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        ProtonSettingsHeader(title = R.string.settings_autofill_section_title)
        ProtonSettingsToggleItem(
            name = stringResource(R.string.settings_autofill_preference_title),
            value = state,
            hint = stringResource(R.string.settings_autofill_preference_description),
            onToggle = onToggleChange
        )
    }
}

class ThemedBooleanPreviewProvider : ThemePairPreviewProvider<Boolean>(BooleanPreviewProvider())

@Preview
@Composable
fun AutofillSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AutofillSection(
                state = input.second,
                onToggleChange = {}
            )
        }
    }
}
