package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.form.NoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
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
    onVaultSelectorClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TitleSection(
            value = aliasItem.title,
            onChange = onTitleChange,
            enabled = isEditAllowed,
            onTitleRequiredError = onTitleRequiredError
        )
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
        MailboxSection(
            contentText = aliasItem.mailboxTitle,
            isEditAllowed = isEditAllowed && aliasItem.mailboxes.size > 1,
            onMailboxClick = onMailboxClick
        )
        NoteSection(
            value = aliasItem.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )
        if (!isUpdate && selectedShare?.name != null) {
            VaultSelector(
                contentText = selectedShare.name,
                isEditAllowed = isEditAllowed,
                onClick = onVaultSelectorClick
            )
        }
    }
}


