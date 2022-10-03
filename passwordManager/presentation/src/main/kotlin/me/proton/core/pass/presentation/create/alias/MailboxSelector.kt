package me.proton.core.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MailboxSelector(
    state: AliasItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = if (state.selectedMailbox != null) {
        state.selectedMailbox.email
    } else {
        ""
    }
    Selector(
        text = value,
        modifier = modifier,
        onClick = onClick
    )
}
