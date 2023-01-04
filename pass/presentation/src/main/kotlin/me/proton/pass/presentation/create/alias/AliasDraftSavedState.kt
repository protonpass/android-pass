package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ShareId

@Stable
sealed interface AliasDraftSavedState {
    object Unknown : AliasDraftSavedState
    data class Success(
        val shareId: ShareId,
        val aliasItem: AliasItem
    ) : AliasDraftSavedState
}
