package me.proton.android.pass.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingViewModel = hiltViewModel(),
    onBoardingShown: () -> Unit
) {
    val onBoardingShown by viewModel.onboardingUiState.collectAsStateWithLifecycle()
    LaunchedEffect(onBoardingShown) {
        if (onBoardingShown) {
            onBoardingShown()
        }
    }
    Box(modifier = modifier) {
        Button(
            onClick = {
                viewModel.onBoardingComplete()
            }
        ) {
            Text(text = "On Boarding Shown")
        }
    }
}
