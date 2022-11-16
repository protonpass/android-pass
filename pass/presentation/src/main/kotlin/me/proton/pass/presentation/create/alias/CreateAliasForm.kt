package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.components.form.NoteInput
import me.proton.pass.presentation.components.form.TitleInput

@Composable
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    state: AliasItem,
    canEdit: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    onTitleChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TitleInput(
            value = state.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        Spacer(Modifier.padding(vertical = 8.dp))

        if (canEdit) {
            CreateAliasSection(
                state = state,
                onChange = onAliasChange,
                onSuffixClick = onSuffixClick,
                canEdit = canEdit,
                onAliasRequiredError = onAliasRequiredError,
                onInvalidAliasError = onInvalidAliasError
            )
        } else {
            DisplayAliasSection(
                state = state
            )
        }
        Spacer(Modifier.padding(vertical = 8.dp))
        MailboxSection(
            state = state,
            onMailboxClick = onMailboxClick
        )
        NoteInput(value = state.note, onChange = onNoteChange)
    }
}
