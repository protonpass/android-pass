package proton.android.pass.featureonboarding.impl

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

data class OnBoardingPageUiState(
    val page: OnBoardingPageName,
    val title: String,
    val subtitle: String,
    val image: @Composable ColumnScope.() -> Unit,
    val mainButton: String,
    val showSkipButton: Boolean
)
