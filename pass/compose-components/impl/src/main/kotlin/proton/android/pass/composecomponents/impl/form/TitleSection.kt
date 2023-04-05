package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
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
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun TitleSection(
    modifier: Modifier = Modifier,
    value: String,
    onTitleRequiredError: Boolean,
    enabled: Boolean = true,
    isRounded: Boolean = false,
    requestFocus: Boolean = false,
    onChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    ProtonTextField(
        modifier = modifier
            .applyIf(
                condition = !isRounded,
                ifTrue = {
                    roundedContainer(ProtonTheme.colors.separatorNorm)
                        .padding(start = 16.dp, top = 10.dp, end = 4.dp, bottom = 10.dp)
                }
            )
            .focusRequester(focusRequester),
        textStyle = PassTypography.hero(enabled),
        label = {
            ProtonTextFieldLabel(
                text = stringResource(id = R.string.field_title_title),
                isError = onTitleRequiredError
            )
        },
        placeholder = {
            ProtonTextFieldPlaceHolder(
                text = stringResource(id = R.string.field_title_hint),
                textStyle = PassTypography.heroWeak,
            )
        },
        trailingIcon = if (value.isNotBlank() && enabled) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        editable = enabled,
        value = value,
        onChange = onChange,
        isError = onTitleRequiredError,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
    }
}

class ThemeAndTitleInputProvider :
    ThemePairPreviewProvider<TitleSectionPreviewData>(TitleSectionPreviewProvider())

@Preview
@Composable
fun TitleInputPreview(
    @PreviewParameter(ThemeAndTitleInputProvider::class) input: Pair<Boolean, TitleSectionPreviewData>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TitleSection(
                value = input.second.title,
                onTitleRequiredError = input.second.onTitleRequiredError,
                enabled = input.second.enabled,
                onChange = {}
            )
        }
    }
}
