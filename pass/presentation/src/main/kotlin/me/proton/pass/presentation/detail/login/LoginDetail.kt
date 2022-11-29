package me.proton.pass.presentation.detail.login

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.pass.domain.Item
import me.proton.pass.presentation.detail.login.bottomsheet.LoginDetailBottomSheetContents

@OptIn(ExperimentalMaterialApi::class)
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
                modifier = modifier.padding(padding),
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

fun openWebsite(context: Context, website: String) {
    runCatching {
        Uri.parse(website)
    }.onSuccess { uri ->
        val i = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
        }
        context.startActivity(i)
    }
}
