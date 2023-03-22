package proton.android.pass.featureaccount.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onSignOutClick: () -> Unit,
    onUpClick: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    AccountContent(
        modifier = modifier,
        state = state,
        onSignOutClick = onSignOutClick,
        onUpClick = onUpClick,
        onManageSubscriptionClick = {
            BrowserUtils.openWebsite(context, "")
        },
        onDeleteAccountClick = {
            BrowserUtils.openWebsite(context, "")
        }
    )
}

