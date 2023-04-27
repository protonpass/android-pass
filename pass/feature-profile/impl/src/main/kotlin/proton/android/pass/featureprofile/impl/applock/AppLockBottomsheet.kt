package proton.android.pass.featureprofile.impl.applock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AppLockBottomsheet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        if (state.event == AppLockEvent.OnChanged) {
            onClose()
        }
    }

    AppLockBottomsheetContent(
        modifier = modifier.bottomSheet(),
        state = state,
        onSelected = { viewModel.onChanged(it) }
    )
}
