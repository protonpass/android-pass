package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.Immutable
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.pass.domain.Item

@Immutable
data class ItemDetailScreenUiState(
    val model: ItemModelUiState?,
    val moreInfoUiState: MoreInfoUiState?
) {
    companion object {
        val Initial = ItemDetailScreenUiState(
            model = null,
            moreInfoUiState = null
        )
    }
}

@Immutable
data class ItemModelUiState(
    val name: String,
    val item: Item
)
