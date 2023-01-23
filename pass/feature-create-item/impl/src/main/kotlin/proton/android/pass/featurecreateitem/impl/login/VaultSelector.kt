package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.ProtonTextTitle
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.alias.Selector

@Composable
internal fun VaultSelector(
    modifier: Modifier = Modifier,
    contentText: String,
    isEditAllowed: Boolean,
    onClick: () -> Unit
) {
    ProtonTextTitle(stringResource(R.string.vault_bottomsheet_title))
    Spacer(modifier = Modifier.height(8.dp))
    Selector(
        modifier = modifier,
        text = contentText,
        enabled = isEditAllowed,
        onClick = onClick
    )
}
