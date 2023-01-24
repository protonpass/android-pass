package proton.android.pass.featurecreateitem.impl.alias

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
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    aliasItem: AliasItem,
    selectedShare: ShareUiModel?,
    canEdit: Boolean,
    isUpdate: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    onTitleChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit,
    onDeleteAliasClick: () -> Unit,
    onVaultSelectorClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TitleInput(
            value = aliasItem.title,
            onChange = onTitleChange,
            enabled = isEditAllowed,
            onTitleRequiredError = onTitleRequiredError
        )
        Spacer(Modifier.padding(vertical = 8.dp))

        if (canEdit) {
            CreateAliasSection(
                state = aliasItem,
                onChange = onAliasChange,
                onSuffixClick = onSuffixClick,
                canEdit = isEditAllowed,
                canSelect = aliasItem.aliasOptions.suffixes.size > 1,
                onAliasRequiredError = onAliasRequiredError,
                onInvalidAliasError = onInvalidAliasError
            )
        } else {
            DisplayAliasSection(
                state = aliasItem
            )
        }
        Spacer(Modifier.padding(vertical = 8.dp))
        MailboxSection(
            contentText = aliasItem.mailboxTitle,
            isEditAllowed = isEditAllowed && aliasItem.mailboxes.size > 1,
            onMailboxClick = onMailboxClick
        )
        NoteInput(
            contentModifier = Modifier.height(100.dp),
            value = aliasItem.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )
        if (!isUpdate && selectedShare?.name != null) {
            Spacer(Modifier.height(height = 20.dp))
            VaultSelector(
                contentText = selectedShare.name,
                isEditAllowed = isEditAllowed,
                onClick = onVaultSelectorClick
            )
        }
        if (isUpdate) {
            Spacer(Modifier.height(height = 24.dp))
            PassOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.action_move_to_trash),
                color = ProtonTheme.colors.notificationError,
                enabled = isEditAllowed,
                onClick = onDeleteAliasClick
            )
        }
    }
}


