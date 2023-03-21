package proton.android.pass.featureprofile.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.BrowserUtils
import java.lang.ref.WeakReference

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ProfileContent(
        modifier = modifier,
        state = state,
        onFingerprintClicked = {
            viewModel.onFingerprintToggle(ContextHolder(WeakReference(context).toOption()), it)
        },
        onAutofillClicked = { viewModel.onToggleAutofill(it) },
        onAccountClick = { },
        onSettingsClick = onSettingsClick,
        onFeedbackClick = onFeedbackClick,
        onRateAppClick = {
            BrowserUtils.openWebsite(
                context,
                "https://play.google.com/store/apps/details?id=proton.android.pass"
            )
        },
        onListClick = onListClick,
        onCreateItemClick = onCreateItemClick
    )
}

