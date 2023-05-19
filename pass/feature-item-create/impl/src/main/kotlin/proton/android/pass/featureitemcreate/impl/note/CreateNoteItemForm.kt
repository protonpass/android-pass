package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    noteItem: NoteItem,
    enabled: Boolean,
    onTitleRequiredError: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    vaultSelect: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        vaultSelect()
        NoteTitle(
            value = noteItem.title,
            requestFocus = true,
            onTitleRequiredError = onTitleRequiredError,
            enabled = enabled,
            onValueChanged = onTitleChange
        )
        FullNoteSection(
            modifier = Modifier.weight(1f),
            textFieldModifier = Modifier.weight(1f),
            enabled = enabled,
            value = noteItem.note,
            onChange = onNoteChange
        )
    }
}
