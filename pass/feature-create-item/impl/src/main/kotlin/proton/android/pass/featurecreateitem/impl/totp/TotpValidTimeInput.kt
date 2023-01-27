package proton.android.pass.featurecreateitem.impl.totp

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.ProtonFormInput
import proton.android.pass.featurecreateitem.impl.R

@Composable
fun TotpValidPeriodInput(
    modifier: Modifier = Modifier,
    value: Int?,
    onChange: (Int?) -> Unit,
    fieldRequiredError: Boolean = false,
    enabled: Boolean = true
) {
    ProtonFormInput(
        modifier = modifier.padding(top = 8.dp),
        title = stringResource(id = R.string.totp_valid_time_field_title),
        placeholder = stringResource(id = R.string.totp_valid_time_field_placeholder),
        editable = enabled,
        value = value?.toString() ?: "",
        onChange = { onChange(it.toIntOrNull()) },
        required = true,
        isError = fieldRequiredError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
