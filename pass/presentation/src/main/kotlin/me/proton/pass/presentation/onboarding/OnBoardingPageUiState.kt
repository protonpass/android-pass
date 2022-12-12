package me.proton.pass.presentation.onboarding

import androidx.compose.runtime.Composable

data class OnBoardingPageUiState(
    val page: OnBoardingPageName,
    val title: String,
    val subtitle: String,
    val image: @Composable () -> Unit,
    val mainButton: String,
    val showSkipButton: Boolean
)
