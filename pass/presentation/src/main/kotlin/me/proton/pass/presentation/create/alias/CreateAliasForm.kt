package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.PassOutlinedButton
import me.proton.pass.presentation.components.form.NoteInput
import me.proton.pass.presentation.components.form.TitleInput
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    state: AliasItem,
    canEdit: Boolean,
    canDelete: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    onTitleChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit,
    onDeleteAliasClick: () -> Unit
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
            enabled = isEditAllowed,
            onTitleRequiredError = onTitleRequiredError
        )
        Spacer(Modifier.padding(vertical = 8.dp))

        if (canEdit) {
            CreateAliasSection(
                state = state,
                onChange = onAliasChange,
                onSuffixClick = onSuffixClick,
                canEdit = canEdit && isEditAllowed,
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
            isEditAllowed = isEditAllowed,
            onMailboxClick = onMailboxClick
        )
        NoteInput(
            contentModifier = Modifier.height(100.dp),
            value = state.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )

        if (canDelete) {
            Spacer(Modifier.height(height = 24.dp))
            PassOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.action_move_to_trash),
                color = ProtonTheme.colors.notificationError,
                enabled = IsButtonEnabled.from(isEditAllowed),
                onClick = onDeleteAliasClick
            )
        }
    }
}


