package me.proton.pass.presentation.components.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.TitleInputPreviewData
import me.proton.pass.presentation.components.previewproviders.TitleInputPreviewProvider

@Composable
fun TitleInput(
    value: String,
    onChange: (String) -> Unit,
    onTitleRequiredError: Boolean
) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp),
        isError = onTitleRequiredError,
        errorMessage = stringResource(id = R.string.field_title_is_blank)
    )
}

@Preview
@Composable
fun TitleInputThemePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    ProtonTheme(isDark = isDarkMode) {
        Surface {
            TitleInput(
                value = "Title input",
                onChange = {},
                onTitleRequiredError = false
            )
        }
    }
}

class ThemeAndTitleInputProvider :
    ThemePairPreviewProvider<TitleInputPreviewData>(TitleInputPreviewProvider())

@Preview
@Composable
fun TitleInputPreview(
    @PreviewParameter(ThemeAndTitleInputProvider::class) input: Pair<Boolean, TitleInputPreviewData>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            TitleInput(
                value = input.second.title,
                onChange = {},
                onTitleRequiredError = input.second.onTitleRequiredError
            )
        }
    }
}
