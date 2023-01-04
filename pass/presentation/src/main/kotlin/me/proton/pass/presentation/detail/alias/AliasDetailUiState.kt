package me.proton.pass.presentation.detail.alias

import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState

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
