package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm

@Composable
fun TitleSection(
    modifier: Modifier = Modifier,
    value: String,
    onTitleRequiredError: Boolean,
    enabled: Boolean = true,
    isRounded: Boolean = false,
    requestFocus: Boolean = false,
    moveToNextOnEnter: Boolean = true,
    onChange: (String) -> Unit,
    onDoneClick: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    ProtonTextField(
        modifier = modifier
            .applyIf(
                condition = !isRounded,
                ifTrue = {
                    roundedContainerNorm()
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
        moveToNextOnEnter = moveToNextOnEnter,
        onDoneClick = onDoneClick,
        isError = onTitleRequiredError,
        errorMessage = stringResource(id = R.string.field_title_required),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            scope.launch {
                delay(DELAY_BEFORE_FOCUS_MS)
                focusRequester.requestFocus()
            }
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

private const val DELAY_BEFORE_FOCUS_MS = 200L
