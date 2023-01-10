package proton.android.pass.featurecreateitem.impl.note

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
import proton.android.pass.composecomponents.impl.buttons.PassOutlinedButton
import proton.android.pass.composecomponents.impl.form.NoteInput
import proton.android.pass.composecomponents.impl.form.TitleInput
import proton.android.pass.featurecreateitem.impl.R
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    state: NoteItem,
    enabled: Boolean,
    canDelete: Boolean,
    onTitleRequiredError: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(
            enabled = enabled,
            value = state.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        NoteInput(
            contentModifier = Modifier.height(300.dp),
            enabled = enabled,
            value = state.note,
            onChange = onNoteChange
        )

        if (canDelete) {
            Spacer(Modifier.height(height = 24.dp))
            PassOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.action_move_to_trash),
                color = ProtonTheme.colors.notificationError,
                onClick = onDelete
            )
        }
    }
}
