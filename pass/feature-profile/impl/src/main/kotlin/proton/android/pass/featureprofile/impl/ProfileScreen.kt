package proton.android.pass.featureprofile.impl

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import java.lang.ref.WeakReference

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onAccountClick: () -> Unit,
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
        onAccountClick = onAccountClick,
        onSettingsClick = onSettingsClick,
        onFeedbackClick = onFeedbackClick,
        onImportExportClick = { openWebsite(context, PASS_IMPORT) },
        onRateAppClick = { openWebsite(context, PASS_STORE) },
        onListClick = onListClick,
        onCreateItemClick = onCreateItemClick,
        onCopyAppVersionClick = { viewModel.copyAppVersion(state.appVersion) },
    )
}

@VisibleForTesting
const val PASS_IMPORT = "https://proton.me/support/pass-import"

@VisibleForTesting
const val PASS_STORE = "https://play.google.com/store/apps/details?id=proton.android.pass"
