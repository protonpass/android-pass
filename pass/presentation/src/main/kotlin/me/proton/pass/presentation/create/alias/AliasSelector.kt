package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AliasSelector(
    modifier: Modifier = Modifier,
    state: AliasItem,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val value = if (state.selectedSuffix != null) {
        state.selectedSuffix.suffix
    } else {
        ""
    }
    Selector(
        modifier = modifier,
        text = value,
        enabled = enabled,
        onClick = onClick
    )
}
