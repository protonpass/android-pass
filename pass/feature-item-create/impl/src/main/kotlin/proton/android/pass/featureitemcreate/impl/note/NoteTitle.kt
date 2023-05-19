package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.coroutines.delay
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.form.ProtonTextField

@Composable
fun NoteTitle(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    requestFocus: Boolean,
    onTitleRequiredError: Boolean,
    onValueChanged: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val titleColor = if (onTitleRequiredError) {
        PassTheme.colors.passwordInteractionNorm
    } else {
        PassTheme.colors.textWeak
    }

    ProtonTextField(
        modifier = modifier
            .focusRequester(focusRequester),
        textStyle = PassTypography.hero(enabled),
        placeholder = {
            Text(
                text = stringResource(id = R.string.field_title_title),
                style = PassTypography.hero,
                color = titleColor
            )
        },
        editable = enabled,
        value = value,
        onChange = onValueChanged,
        singleLine = false,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            delay(DELAY_BEFORE_FOCUS_MS)
            focusRequester.requestFocus()
        }
    }
}

class ThemeNoteTitlePreviewProvider :
    ThemePairPreviewProvider<NoteTitleInput>(NoteTitlePreviewProvider())

@Preview
@Composable
fun NoteTitlePreview(
    @PreviewParameter(ThemeNoteTitlePreviewProvider::class) input: Pair<Boolean, NoteTitleInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteTitle(
                value = input.second.text,
                enabled = input.second.enabled,
                onTitleRequiredError = input.second.isError,
                requestFocus = false,
                onValueChanged = {}
            )
        }
    }
}

private const val DELAY_BEFORE_FOCUS_MS = 200L
