package proton.android.pass.featureitemcreate.impl.note

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
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    noteItem: NoteItem,
    selectedShare: ShareUiModel?,
    enabled: Boolean,
    showVaultSelector: Boolean,
    onTitleRequiredError: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onVaultSelectorClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleVaultSelectionSection(
            enabled = enabled,
            titleValue = noteItem.title,
            showVaultSelector = showVaultSelector,
            vaultName = selectedShare?.name,
            vaultColor = selectedShare?.color,
            vaultIcon = selectedShare?.icon,
            onTitleChanged = onTitleChange,
            onTitleRequiredError = onTitleRequiredError,
            onVaultClicked = onVaultSelectorClick
        )
        NoteSection(
            enabled = enabled,
            isRounded = true,
            value = noteItem.note,
            onChange = onNoteChange
        )
    }
}
