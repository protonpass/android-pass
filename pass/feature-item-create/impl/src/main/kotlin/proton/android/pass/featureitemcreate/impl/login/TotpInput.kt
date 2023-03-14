package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun TotpInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    onTotpChanged: (String) -> Unit,
    onFocus: (Boolean) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(0.dp, 16.dp),
        value = value,
        onChange = onTotpChanged,
        editable = enabled,
        textStyle = ProtonTheme.typography.default,
        onFocusChange = onFocus,
        label = { ProtonTextFieldLabel(text = stringResource(R.string.totp_create_login_field_title)) },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.totp_create_login_field_placeholder))
        },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
                contentDescription = "",
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                SmallCrossIconButton(enabled = enabled) { onTotpChanged("") }
            }
        }
    )
}
