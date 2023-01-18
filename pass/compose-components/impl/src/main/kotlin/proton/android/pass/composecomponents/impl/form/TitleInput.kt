package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun TitleInput(
    value: String,
    onChange: (String) -> Unit,
    onTitleRequiredError: Boolean,
    enabled: Boolean = true
) {
    ProtonFormInput(
        title = stringResource(id = R.string.field_title_title),
        placeholder = stringResource(id = R.string.field_title_hint),
        editable = enabled,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp),
        isError = onTitleRequiredError,
        errorMessage = stringResource(id = R.string.field_title_is_blank),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
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
                onTitleRequiredError = input.second.onTitleRequiredError,
                enabled = input.second.enabled,
                onChange = {}
            )
        }
    }
}
