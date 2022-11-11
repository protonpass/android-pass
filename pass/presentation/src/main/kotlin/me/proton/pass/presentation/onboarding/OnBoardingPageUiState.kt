package me.proton.pass.presentation.onboarding

data class OnBoardingPageUiState(
    val page: OnBoardingPageName,
    val title: String,
    val subtitle: String,
    val mainButton: String
)
