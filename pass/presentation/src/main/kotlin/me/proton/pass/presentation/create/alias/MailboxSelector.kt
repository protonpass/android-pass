package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MailboxSelector(
    modifier: Modifier = Modifier,
    contentText: String,
    isEditAllowed: Boolean,
    onClick: () -> Unit
) {
    Selector(
        modifier = modifier,
        text = contentText,
        enabled = isEditAllowed,
        onClick = onClick
    )
}
