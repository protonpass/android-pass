package me.proton.pass.presentation.onboarding

import androidx.compose.runtime.Stable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class OnBoardingUiState(
    val selectedPage: Int,
    val enabledPages: Set<OnBoardingPageName>,
    val isCompleted: Boolean
) {
    companion object {
        val Initial = OnBoardingUiState(0, emptySet(), false)
    }
}

@Stable
enum class OnBoardingPageName {
    Autofill, Fingerprint
}

open class OnBoardingUiStatePreviewProvider : PreviewParameterProvider<OnBoardingUiState> {
    override val values: Sequence<OnBoardingUiState> = sequenceOf(
        OnBoardingUiState(0, setOf(OnBoardingPageName.Autofill), false),
        OnBoardingUiState(0, setOf(OnBoardingPageName.Fingerprint), false)
    )
}
