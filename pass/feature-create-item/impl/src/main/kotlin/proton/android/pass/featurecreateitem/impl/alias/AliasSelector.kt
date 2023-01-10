package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AliasSelector(
    modifier: Modifier = Modifier,
    contentText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Selector(
        modifier = modifier,
        text = contentText,
        enabled = enabled,
        onClick = onClick
    )
}
