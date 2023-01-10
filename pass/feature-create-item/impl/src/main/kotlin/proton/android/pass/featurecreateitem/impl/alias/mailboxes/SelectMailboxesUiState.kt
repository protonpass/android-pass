package proton.android.pass.featurecreateitem.impl.alias.mailboxes

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.featurecreateitem.impl.alias.SelectedAliasMailboxUiModel

@Stable
data class SelectMailboxesUiState(
    val mailboxes: List<SelectedAliasMailboxUiModel>,
    val canApply: IsButtonEnabled
) {
    companion object {
        val Initial = SelectMailboxesUiState(
            mailboxes = emptyList(),
            canApply = IsButtonEnabled.Disabled
        )
    }
}
