package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.ProtonFormInput
import proton.android.pass.featurecreateitem.impl.R

@Composable
fun AliasInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    editable: Boolean
) {

    val errorMessage = if (onAliasRequiredError) {
        stringResource(R.string.field_alias_is_blank)
    } else if (onInvalidAliasError) {
        stringResource(R.string.field_alias_invalid)
    } else {
        ""
    }

    ProtonFormInput(
        modifier = modifier.padding(top = 8.dp),
        title = stringResource(id = R.string.field_alias_title),
        placeholder = stringResource(id = R.string.field_alias_hint),
        value = value,
        onChange = onChange,
        required = true,
        isError = onAliasRequiredError || onInvalidAliasError,
        editable = editable,
        errorMessage = errorMessage
    )
}
