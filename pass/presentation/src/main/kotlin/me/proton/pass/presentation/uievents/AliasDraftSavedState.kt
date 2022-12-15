package me.proton.pass.presentation.uievents

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.alias.AliasItem

@Stable
sealed interface AliasDraftSavedState {
    object Unknown : AliasDraftSavedState
    data class Success(
        val shareId: ShareId,
        val aliasItem: AliasItem
    ) : AliasDraftSavedState
}
