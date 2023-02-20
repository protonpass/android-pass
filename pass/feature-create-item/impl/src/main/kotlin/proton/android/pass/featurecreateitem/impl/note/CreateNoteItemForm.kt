package proton.android.pass.featurecreateitem.impl.note

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
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    noteItem: NoteItem,
    selectedShare: ShareUiModel?,
    enabled: Boolean,
    isUpdate: Boolean,
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
        TitleSection(
            enabled = enabled,
            isRounded = true,
            value = noteItem.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        NoteSection(
            enabled = enabled,
            isRounded = true,
            value = noteItem.note,
            onChange = onNoteChange
        )
        if (!isUpdate) {
            selectedShare?.name?.let {
                VaultSelector(
                    contentText = it,
                    isEditAllowed = true,
                    onClick = onVaultSelectorClick
                )
            }
        }
    }
}
