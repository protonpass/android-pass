package proton.android.pass.featuretrial.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun TrialScreen(
    modifier: Modifier = Modifier,
    onNavigate: (TrialNavigation) -> Unit,
    viewModel: TrialViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TrialScreenContent(
        modifier = modifier,
        state = state,
        onNavigate = onNavigate
    )
}
