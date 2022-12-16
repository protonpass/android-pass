package me.proton.pass.presentation.create.alias.mailboxes

import androidx.compose.runtime.Stable
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Stable
data class SelectMailboxesUiState(
    val mailboxes: List<AliasMailboxUiModel>,
    val canApply: IsButtonEnabled
) {
    companion object {
        val Initial = SelectMailboxesUiState(
            mailboxes = emptyList(),
            canApply = IsButtonEnabled.Disabled
        )
    }
}
