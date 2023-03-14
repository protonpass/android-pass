package proton.android.pass.featureitemcreate.impl.alias

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
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection

@Composable
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    aliasItem: AliasItem,
    selectedShare: ShareUiModel?,
    canEdit: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    showVaultSelector: Boolean,
    onTitleChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
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
        TitleVaultSelectionSection(
            titleValue = aliasItem.title,
            onTitleChanged = onTitleChange,
            onTitleRequiredError = onTitleRequiredError,
            enabled = isEditAllowed,
            showVaultSelector = showVaultSelector,
            vaultName = selectedShare?.name,
            vaultIcon = selectedShare?.icon,
            vaultColor = selectedShare?.color,
            onVaultClicked = onVaultSelectorClick
        )
        if (canEdit) {
            CreateAliasSection(
                state = aliasItem,
                onChange = onPrefixChange,
                onSuffixClick = onSuffixClick,
                canEdit = isEditAllowed,
                canSelectSuffix = aliasItem.aliasOptions.suffixes.size > 1,
                onAliasRequiredError = onAliasRequiredError,
                onInvalidAliasError = onInvalidAliasError
            )
        } else {
            DisplayAliasSection(
                state = aliasItem,
                isLoading = isLoading
            )
        }
        MailboxSection(
            mailboxes = aliasItem.mailboxes,
            isEditAllowed = isEditAllowed && aliasItem.mailboxes.size > 1,
            isLoading = isLoading,
            onMailboxClick = onMailboxClick
        )
        NoteSection(
            value = aliasItem.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )
    }
}


