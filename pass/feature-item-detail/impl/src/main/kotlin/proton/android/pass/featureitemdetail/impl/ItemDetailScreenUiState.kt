package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.Immutable
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState

@Immutable
data class ItemDetailScreenUiState(
    val itemTypeUiState: ItemTypeUiState,
    val moreInfoUiState: MoreInfoUiState
) {
    companion object {
        val Initial = ItemDetailScreenUiState(
            itemTypeUiState = ItemTypeUiState.Unknown,
            moreInfoUiState = MoreInfoUiState.Initial
        )
    }
}

enum class ItemTypeUiState {
    Login,
    Note,
    Alias,
    Password,
    Unknown
}
