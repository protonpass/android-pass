package me.proton.pass.presentation.create.alias.mailboxes

import androidx.compose.runtime.Stable
import me.proton.pass.presentation.create.alias.SelectedAliasMailboxUiModel
import me.proton.pass.presentation.uievents.IsButtonEnabled

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
