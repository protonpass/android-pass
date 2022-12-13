package me.proton.pass.presentation.detail.login

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.common.bottomsheet.PassModalBottomSheetLayout
import me.proton.pass.presentation.detail.login.bottomsheet.LoginDetailBottomSheetContents
import me.proton.pass.presentation.utils.BrowserUtils.openWebsite

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
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            LoginDetailBottomSheetContents(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
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
            modifier = modifier,
            topBar = topBar
        ) { padding ->
            LoginContent(
                modifier = Modifier.padding(padding),
                model = model,
                onTogglePasswordClick = { viewModel.togglePassword() },
                onCopyPasswordClick = { viewModel.copyPasswordToClipboard() },
                onUsernameClick = { viewModel.copyUsernameToClipboard() },
                onWebsiteClicked = { website -> openWebsite(context, website) },
                onWebsiteLongClicked = { website ->
                    setSelectedWebsite(website)
                    scope.launch { bottomSheetState.show() }
                }
            )
        }
    }
}

