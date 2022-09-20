package me.proton.android.pass.ui.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AliasSelector(
    state: BaseAliasViewModel.ModelState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = if (state.selectedSuffix != null) {
        state.selectedSuffix.suffix
    } else {
        ""
    }
    Selector(
        text = value,
        modifier = modifier,
        onClick = onClick
    )
}
