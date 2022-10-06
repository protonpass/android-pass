package me.proton.core.pass.presentation.create.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.form.ProtonFormInput

@Composable
fun AliasInput(
    value: String,
    onChange: (String) -> Unit,
    onAliasRequiredError: Boolean,
    editable: Boolean
) {
    ProtonFormInput(
        title = R.string.field_alias_title,
        placeholder = R.string.field_alias_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp),
        isError = onAliasRequiredError,
        editable = editable,
        errorMessage = stringResource(id = R.string.field_alias_is_blank)
    )
}
