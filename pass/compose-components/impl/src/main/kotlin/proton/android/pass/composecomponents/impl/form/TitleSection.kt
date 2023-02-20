package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
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
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier
            .applyIf(
                condition = !isRounded,
                ifTrue = {
                    roundedContainer(ProtonTheme.colors.separatorNorm)
                        .padding(16.dp, 10.dp, 0.dp, 10.dp)
                }
            ),
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
        trailingIcon = if (!isRounded && value.isNotBlank() && enabled) {
            {
                IconButton(onClick = { onChange("") }) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                        contentDescription = stringResource(R.string.clear_title_icon_content_description),
                        tint = ProtonTheme.colors.iconWeak,
                    )
                }
            }
        } else {
            null
        },
        editable = enabled,
        value = value,
        onChange = onChange,
        isError = onTitleRequiredError,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

class ThemeAndTitleInputProvider :
    ThemePairPreviewProvider<TitleSectionPreviewData>(TitleSectionPreviewProvider())

@Preview
@Composable
fun TitleInputPreview(
    @PreviewParameter(ThemeAndTitleInputProvider::class) input: Pair<Boolean, TitleSectionPreviewData>
) {
    ProtonTheme(isDark = input.first) {
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
