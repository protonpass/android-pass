package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.HiddenState

@Composable
internal fun TotpInput(
    modifier: Modifier = Modifier,
    value: HiddenState,
    label: String = stringResource(R.string.totp_create_login_field_title),
    enabled: Boolean,
    isError: Boolean,
    onTotpChanged: (String) -> Unit,
    onFocus: (Boolean) -> Unit
) {
    val text = when (value) {
        is HiddenState.Concealed -> "Concealed"
        is HiddenState.Revealed -> value.clearText
    }
    ProtonTextField(
        modifier = modifier.padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = text,
        onChange = onTotpChanged,
        editable = enabled,
        isError = isError,
        errorMessage = stringResource(id = R.string.totp_create_login_field_invalid),
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm,
        onFocusChange = onFocus,
        label = { ProtonTextFieldLabel(text = label, isError = isError) },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.totp_create_login_field_placeholder))
        },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
                contentDescription = stringResource(R.string.mfa_icon_content_description),
                tint = if (isError) {
                    PassTheme.colors.signalDanger
                } else {
                    ProtonTheme.colors.iconWeak
                }
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                SmallCrossIconButton(enabled = enabled) { onTotpChanged("") }
            }
        }
    )
}

@Preview
@Composable
fun TotpInputPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TotpInput(
                value = HiddenState.Revealed("", "123"),
                enabled = true,
                isError = input.second,
                onTotpChanged = {},
                onFocus = { }
            )
        }
    }
}
