package proton.android.pass.featurecreateitem.impl.totp

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.ProtonFormInput
import proton.android.pass.featurecreateitem.impl.R

@Composable
fun TotpLabelInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    fieldRequiredError: Boolean = false,
    enabled: Boolean = true
) {
    ProtonFormInput(
        modifier = modifier.padding(top = 8.dp),
        title = stringResource(id = R.string.totp_label_field_title),
        placeholder = stringResource(id = R.string.totp_label_field_placeholder),
        editable = enabled,
        value = value,
        onChange = onChange,
        required = true,
        isError = fieldRequiredError,
    )
}
