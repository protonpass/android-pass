package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun AliasInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    editable: Boolean
) {
    ProtonTextField(
        modifier = modifier.padding(top = 8.dp),
        textStyle = ProtonTheme.typography.default(editable),
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_alias_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_alias_hint)) },
        value = value,
        onChange = onChange,
        isError = onAliasRequiredError || onInvalidAliasError,
        editable = editable
    )
}
