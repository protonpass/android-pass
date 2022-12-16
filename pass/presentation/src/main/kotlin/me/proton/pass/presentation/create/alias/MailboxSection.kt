package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonTextTitle

@Composable
internal fun MailboxSection(
    modifier: Modifier = Modifier,
    state: AliasItem,
    isEditAllowed: Boolean,
    onMailboxClick: () -> Unit
) {
    Column(modifier = modifier) {
        ProtonTextTitle(R.string.field_mailboxes_title)
        MailboxSelector(
            modifier = Modifier.padding(top = 8.dp),
            state = state,
            isEditAllowed = isEditAllowed,
            onClick = onMailboxClick
        )
    }
}
