package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.NoteSection
import proton.android.pass.composecomponents.impl.form.VaultSelector
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

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
        if (showVaultSelector) {
            Column { // Column so spacedBy does not affect the spacer
                VaultSelector(
                    modifier = Modifier.roundedContainer(ProtonTheme.colors.separatorNorm),
                    vaultName = selectedShare?.name ?: "",
                    color = selectedShare?.color ?: ShareColor.Color1,
                    icon = selectedShare?.icon ?: ShareIcon.Icon1,
                    onVaultClicked = onVaultSelectorClick
                )
                Spacer(modifier = Modifier.height(8.dp)) // 16 come from spacedBy + 8 = 24
            }
        }
        NoteTitle(
            value = noteItem.title,
            requestFocus = true,
            onTitleRequiredError = onTitleRequiredError,
            enabled = enabled,
            onValueChanged = onTitleChange
        )
        NoteSection(
            modifier = modifier.weight(1f),
            enabled = enabled,
            isRounded = true,
            value = noteItem.note,
            onChange = onNoteChange
        )
    }
}
