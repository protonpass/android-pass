package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.Selector

@Composable
internal fun VaultSelector(
    modifier: Modifier = Modifier,
    contentText: String,
    isEditAllowed: Boolean,
    onClick: () -> Unit
) {
    ProtonTextFieldLabel(text = stringResource(R.string.vault_bottomsheet_title))
    Selector(
        modifier = modifier,
        text = contentText,
        enabled = isEditAllowed,
        onClick = onClick
    )
}
