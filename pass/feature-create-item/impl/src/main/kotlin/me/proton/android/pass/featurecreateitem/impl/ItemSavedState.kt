package me.proton.android.pass.featurecreateitem.impl

import androidx.compose.runtime.Stable
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.pass.domain.ItemId

@Stable
sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(
        val itemId: ItemId,
        val item: ItemUiModel
    ) : ItemSavedState
}
