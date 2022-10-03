package me.proton.core.pass.presentation.create.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.android.pass.ui.shared.ProtonTextTitle
import me.proton.core.pass.presentation.R

@Composable
internal fun MailboxSection(
    state: AliasItem,
    onMailboxClick: () -> Unit
) {
    ProtonTextTitle(R.string.field_mailboxes_title)
    MailboxSelector(
        state = state,
        modifier = Modifier.padding(top = 8.dp),
        onClick = onMailboxClick
    )
}
