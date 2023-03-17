package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        style = PassTypography.hero
                    )
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Profile,
                onListClick = onListClick,
                onCreateClick = onCreateItemClick,
                onProfileClick = {}
            )
        }
    ) { padding ->
        val context = LocalContext.current
        ProfileContent(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            state = state,
            onFingerprintClicked = {
                viewModel.onFingerprintToggle(ContextHolder(WeakReference(context).toOption()), it)
            },
            onAutofillClicked = { viewModel.onToggleAutofill(it) },
            onAccountClick = { },
            onSettingsClick = onSettingsClick,
            onTipsClick = {},
            onFeedbackClick = onFeedbackClick,
            onRateAppClick = {}
        )
    }
}

