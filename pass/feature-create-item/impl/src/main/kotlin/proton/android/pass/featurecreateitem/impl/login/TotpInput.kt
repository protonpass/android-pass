package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import proton.android.pass.featurecreateitem.impl.R

@Composable
internal fun TotpInput(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    onAddTotpClick: () -> Unit,
    onDeleteTotpClick: () -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(0.dp, 16.dp),
        value = value,
        onChange = { },
        editable = false,
        textStyle = ProtonTheme.typography.default,
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
            IconButton(
                enabled = enabled,
                onClick = {
                    if (value.isNotBlank()) {
                        onDeleteTotpClick()
                    } else {
                        onAddTotpClick()
                    }
                }
            ) {
                val buttonIcon = if (value.isNotBlank()) {
                    me.proton.core.presentation.R.drawable.ic_proton_trash
                } else {
                    me.proton.core.presentation.R.drawable.ic_proton_plus
                }
                Icon(
                    painter = painterResource(buttonIcon),
                    contentDescription = null,
                    tint = if (enabled) {
                        ProtonTheme.colors.iconNorm
                    } else {
                        ProtonTheme.colors.iconDisabled
                    }
                )
            }
        }
    )
}
