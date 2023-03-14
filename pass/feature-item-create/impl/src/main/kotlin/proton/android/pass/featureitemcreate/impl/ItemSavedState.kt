package proton.android.pass.featureitemcreate.impl

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId

@Stable
sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(
        val itemId: ItemId,
        val item: ItemUiModel
    ) : ItemSavedState
}
