package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.LoginDetailBottomSheetContents
import proton.pass.domain.Item

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun LoginDetail(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    item: Item,
    viewModel: LoginDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val model by viewModel.viewState.collectAsStateWithLifecycle()

    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val (selectedWebsite, setSelectedWebsite) = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            LoginDetailBottomSheetContents(
                website = selectedWebsite,
                onCopyToClipboard = { website ->
                    viewModel.copyWebsiteToClipboard(website)
                    scope.launch { bottomSheetState.hide() }
                },
                onOpenWebsite = { website ->
                    openWebsite(context, website)
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = topBar
        ) { padding ->
            LoginContent(
                modifier = modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                state = model,
                onTogglePasswordClick = { viewModel.togglePassword() },
                onCopyPasswordClick = { viewModel.copyPasswordToClipboard() },
                onUsernameClick = { viewModel.copyUsernameToClipboard() },
                onWebsiteClicked = { website -> openWebsite(context, website) },
                onWebsiteLongClicked = { website ->
                    setSelectedWebsite(website)
                    scope.launch { bottomSheetState.show() }
                },
                onCopyTotpClick = {
                    viewModel.copyTotpCodeToClipboard(it)
                }
            )
        }
    }
}

