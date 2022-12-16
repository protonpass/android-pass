package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MailboxSelector(
    modifier: Modifier = Modifier,
    state: AliasItem,
    isEditAllowed: Boolean,
    onClick: () -> Unit
) {
    Selector(
        modifier = modifier,
        text = state.mailboxTitle,
        enabled = isEditAllowed,
        onClick = onClick
    )
}
