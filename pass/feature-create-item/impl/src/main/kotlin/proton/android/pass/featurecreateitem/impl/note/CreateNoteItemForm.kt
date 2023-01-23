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
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.buttons.PassOutlinedButton
import proton.android.pass.composecomponents.impl.form.NoteInput
import proton.android.pass.composecomponents.impl.form.TitleInput
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.login.VaultSelector

@Composable
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    noteItem: NoteItem,
    selectedShare: ShareUiModel?,
    enabled: Boolean,
    isUpdate: Boolean,
    onTitleRequiredError: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onVaultSelectorClick: () -> Unit,
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
            value = noteItem.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        NoteInput(
            contentModifier = Modifier.height(300.dp),
            enabled = enabled,
            value = noteItem.note,
            onChange = onNoteChange
        )

        if (!isUpdate) {
            selectedShare?.name?.let {
                Spacer(Modifier.height(height = 20.dp))
                VaultSelector(
                    contentText = it,
                    isEditAllowed = true,
                    onClick = onVaultSelectorClick
                )
            }
        }
        if (isUpdate) {
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
