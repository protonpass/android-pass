package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun PasswordInput(
    modifier: Modifier = Modifier,
    value: String,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onFocus: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val visualTransformation = if (isFocused) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    ProtonTextField(
        modifier = modifier.padding(0.dp, 16.dp),
        value = value,
        editable = isEditAllowed,
        textStyle = ProtonTheme.typography.default(isEditAllowed),
        onChange = onChange,
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_password_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_password_hint)) },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_key),
                contentDescription = "",
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        onFocusChange = {
            isFocused = it
            onFocus(it)
        }
    )
}

@Preview
@Composable
fun PasswordInputPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PasswordInput(
                value = "someValue",
                isEditAllowed = input.second,
                onChange = {},
                onFocus = {}
            )
        }
    }
}
