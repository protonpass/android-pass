package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonFormInput

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
        title = R.string.field_alias_title,
        placeholder = R.string.field_alias_hint,
        value = value,
        onChange = onChange,
        required = true,
        isError = onAliasRequiredError || onInvalidAliasError,
        editable = editable,
        errorMessage = errorMessage
    )
}
