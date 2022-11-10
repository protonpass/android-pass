package me.proton.android.pass.ui.detail.alias

import me.proton.pass.presentation.uievents.IsLoadingState

data class AliasDetailUiState(
    val isLoadingState: IsLoadingState,
    val model: AliasUiModel?
) {
    companion object {
        val Initial = AliasDetailUiState(
            isLoadingState = IsLoadingState.Loading,
            model = null
        )
    }
}
