package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.Stable
import proton.pass.domain.ItemId

@Stable
sealed interface AliasSavedState {
    object Unknown : AliasSavedState
    data class Success(val itemId: ItemId, val alias: String) : AliasSavedState
}
