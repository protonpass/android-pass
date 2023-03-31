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
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.TopBarOptions
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.WebsiteOptions
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.WebsiteOptionsBottomSheetContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun LoginDetail(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    viewModel: LoginDetailViewModel = hiltViewModel(),
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMigrateClick: (ShareId, ItemId) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        LoginDetailUiState.NotInitialised -> {}
        LoginDetailUiState.Error -> LaunchedEffect(Unit) { onUpClick() }
        is LoginDetailUiState.Success -> {
            if (state.isItemSentToTrash) {
                LaunchedEffect(Unit) { onUpClick() }
            }
            val bottomSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                skipHalfExpanded = true
            )
            var currentBottomSheet by remember { mutableStateOf(WebsiteOptions) }
            var selectedWebsite by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            PassModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                    when (currentBottomSheet) {
                        WebsiteOptions -> {
                            WebsiteOptionsBottomSheetContents(
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
                        TopBarOptions -> when (state.state) {
                            ItemState.Active.value -> TopBarOptionsBottomSheetContents(
                                onMigrate = {
                                    scope.launch { bottomSheetState.hide() }
                                    onMigrateClick(state.shareId, state.itemId)
                                },
                                onMoveToTrash = {
                                    viewModel.onDelete(state.shareId, state.itemId)
                                    scope.launch { bottomSheetState.hide() }
                                }
                            )
                            ItemState.Trashed.value -> {}
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        ItemDetailTopBar(
                            isLoading = state.isLoading,
                            isInTrash = state.state == ItemState.Trashed.value,
                            color = PassTheme.colors.loginInteractionNormMajor1,
                            onUpClick = onUpClick,
                            onEditClick = {
                                onEditClick(state.shareId, state.itemId, state.itemType)
                            },
                            onOptionsClick = {
                                currentBottomSheet = TopBarOptions
                                scope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                ) { padding ->
                    LoginContent(
                        modifier = modifier
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        state = state,
                        moreInfoUiState = moreInfoUiState,
                        onTogglePasswordClick = { viewModel.togglePassword() },
                        onCopyPasswordClick = { viewModel.copyPasswordToClipboard() },
                        onUsernameClick = { viewModel.copyUsernameToClipboard() },
                        onWebsiteClicked = { website -> openWebsite(context, website) },
                        onWebsiteLongClicked = { website ->
                            selectedWebsite = website
                            currentBottomSheet = WebsiteOptions
                            scope.launch { bottomSheetState.show() }
                        },
                        onCopyTotpClick = {
                            viewModel.copyTotpCodeToClipboard(it)
                        }
                    )
                }
            }
        }
    }
}

