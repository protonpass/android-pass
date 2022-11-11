package me.proton.pass.presentation.onboarding

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class OnBoardingUiState(
    val selectedPage: Int,
    val pageCount: Int,
    val isCompleted: Boolean
) {
    companion object {
        val Initial = OnBoardingUiState(0, 2, false)
    }
}

open class OnBoardingUiStatePreviewProvider : PreviewParameterProvider<OnBoardingUiState> {
    override val values: Sequence<OnBoardingUiState> = sequenceOf(
        OnBoardingUiState.Initial,
        OnBoardingUiState(0, 1, false)
    )
}
