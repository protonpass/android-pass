package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun TotpSecretInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    fieldRequiredError: Boolean = false,
    enabled: Boolean = true
) {
    ProtonTextField(
        modifier = modifier.padding(top = 8.dp),
        textStyle = ProtonTheme.typography.default(enabled),
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.totp_secret_field_title)) },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.totp_secret_field_placeholder))
        },
        editable = enabled,
        value = value,
        onChange = onChange,
        isError = fieldRequiredError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
