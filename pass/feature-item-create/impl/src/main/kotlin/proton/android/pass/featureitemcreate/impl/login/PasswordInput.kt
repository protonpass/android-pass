package proton.android.pass.featureitemcreate.impl.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.HiddenState

private const val PASSWORD_CONCEALED_LENGTH = 8

@Composable
internal fun PasswordInput(
    modifier: Modifier = Modifier,
    value: HiddenState,
    label: String = stringResource(id = R.string.field_password_title),
    placeholder: String = stringResource(id = R.string.field_password_hint),
    @DrawableRes icon: Int = me.proton.core.presentation.R.drawable.ic_proton_key,
    iconContentDescription: String = "",
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onFocus: (Boolean) -> Unit
) {
    val (text, visualTransformation) = when (value) {
        is HiddenState.Concealed -> "x".repeat(PASSWORD_CONCEALED_LENGTH) to PasswordVisualTransformation()
        is HiddenState.Revealed -> value.clearText to VisualTransformation.None
        is HiddenState.Empty -> "" to VisualTransformation.None
    }
    ProtonTextField(
        modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = text,
        editable = isEditAllowed,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
        onChange = onChange,
        label = { ProtonTextFieldLabel(text = label) },
        placeholder = { ProtonTextFieldPlaceHolder(text = placeholder) },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = iconContentDescription,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = if (value is HiddenState.Revealed && text.isNotEmpty()) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        onFocusChange = { onFocus(it) }
    )
}

class ThemePasswordInputPreviewProvider :
    ThemePairPreviewProvider<PasswordInputPreviewParams>(PasswordInputPreviewProvider())

@Preview
@Composable
fun PasswordInputPreview(
    @PreviewParameter(ThemePasswordInputPreviewProvider::class) input: Pair<Boolean, PasswordInputPreviewParams>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PasswordInput(
                value = input.second.hiddenState,
                isEditAllowed = input.second.isEditAllowed,
                onChange = {},
                onFocus = {}
            )
        }
    }
}
