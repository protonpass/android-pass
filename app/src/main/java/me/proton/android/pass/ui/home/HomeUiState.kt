package me.proton.android.pass.ui.home

import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Content(
        val items: List<ItemUiModel>,
        val selectedShare: ShareId? = null,
        val searchQuery: String
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
