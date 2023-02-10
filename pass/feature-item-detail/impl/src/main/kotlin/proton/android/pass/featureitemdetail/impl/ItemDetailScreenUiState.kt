package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.Immutable
import proton.pass.domain.Item

@Immutable
data class ItemDetailScreenUiState(
    val model: ItemModelUiState?
) {
    companion object {
        val Initial = ItemDetailScreenUiState(
            model = null
        )
    }
}

@Immutable
data class ItemModelUiState(
    val name: String,
    val item: Item
)
