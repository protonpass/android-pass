package me.proton.core.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MailboxSelector(
    state: AliasItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Selector(
        text = state.mailboxTitle,
        modifier = modifier,
        onClick = onClick
    )
}
