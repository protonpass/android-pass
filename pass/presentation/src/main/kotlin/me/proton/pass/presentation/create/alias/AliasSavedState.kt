package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ItemId

@Stable
sealed interface AliasSavedState {
    object Unknown : AliasSavedState
    data class Success(val itemId: ItemId, val alias: String) : AliasSavedState
}
