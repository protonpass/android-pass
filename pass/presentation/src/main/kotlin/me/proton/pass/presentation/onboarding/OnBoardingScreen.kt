package me.proton.pass.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import me.proton.android.pass.biometry.ContextHolder

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingViewModel = hiltViewModel(),
    onBoardingShown: () -> Unit
) {
    val onBoardingUiState by viewModel.onBoardingUiState.collectAsStateWithLifecycle()
    LaunchedEffect(onBoardingUiState.isCompleted) {
        if (onBoardingUiState.isCompleted) {
            onBoardingShown()
        }
    }
    val context = LocalContext.current
    OnBoardingContent(
        modifier = modifier,
        uiState = onBoardingUiState,
        onMainButtonClick = { viewModel.onMainButtonClick(it, ContextHolder.fromContext(context)) },
        onSkipButtonClick = viewModel::onSkipButtonClick,
        onSelectedPageChanged = viewModel::onSelectedPageChanged
    )
}
