package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LogViewScreen(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    viewModel: LogViewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadLogFile(context)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LogViewContent(
        modifier = modifier,
        content = state,
        onUpClick = onUpClick,
        onShareLogsClick = { viewModel.startShareIntent(context) }
    )
}
