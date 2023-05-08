package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.TopBarOptions
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.WebsiteOptions
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.WebsiteOptionsBottomSheetContents
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemState

@OptIn(
    ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun LoginDetail(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    viewModel: LoginDetailViewModel = hiltViewModel(),
    onNavigate: (ItemDetailNavigation) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        LoginDetailUiState.NotInitialised -> {}
        LoginDetailUiState.Error -> LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
        is LoginDetailUiState.Success -> {
            var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
            if (state.isItemSentToTrash || state.isPermanentlyDeleted || state.isRestoredFromTrash) {
                LaunchedEffect(Unit) { onNavigate(ItemDetailNavigation.Back) }
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
                        WebsiteOptions -> WebsiteOptionsBottomSheetContents(
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
                        TopBarOptions -> when (state.itemUiModel.state) {
                            ItemState.Active.value -> TopBarOptionsBottomSheetContents(
                                onMigrate = {
                                    scope.launch {
                                        bottomSheetState.hide()
                                        onNavigate(
                                            ItemDetailNavigation.OnMigrate(
                                                shareId = state.itemUiModel.shareId,
                                                itemId = state.itemUiModel.id,
                                            )
                                        )
                                    }

                                },
                                onMoveToTrash = {
                                    viewModel.onMoveToTrash(
                                        state.itemUiModel.shareId,
                                        state.itemUiModel.id
                                    )
                                    scope.launch { bottomSheetState.hide() }
                                }
                            )
                            ItemState.Trashed.value -> TrashItemBottomSheetContents(
                                itemUiModel = state.itemUiModel,
                                onRestoreItem = { shareId, itemId ->
                                    scope.launch { bottomSheetState.hide() }
                                    viewModel.onItemRestore(shareId, itemId)
                                },
                                onDeleteItem = { _, _ ->
                                    scope.launch { bottomSheetState.hide() }
                                    shouldShowDeleteItemDialog = true
                                }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    modifier = modifier,
                    topBar = {
                        ItemDetailTopBar(
                            isLoading = state.isLoading,
                            isInTrash = state.itemUiModel.state == ItemState.Trashed.value,
                            actionColor = PassTheme.colors.loginInteractionNormMajor1,
                            iconColor = PassTheme.colors.loginInteractionNormMajor2,
                            iconBackgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                            onUpClick = { onNavigate(ItemDetailNavigation.Back) },
                            onEditClick = {
                                onNavigate(
                                    ItemDetailNavigation.OnEdit(
                                        shareId = state.itemUiModel.shareId,
                                        itemId = state.itemUiModel.id,
                                        itemType = state.itemUiModel.itemType
                                    )
                                )
                            },
                            onOptionsClick = {
                                currentBottomSheet = TopBarOptions
                                scope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                ) { padding ->
                    LoginContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PassTheme.colors.itemDetailBackground)
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        itemUiModel = state.itemUiModel,
                        vault = state.vault,
                        showViewAlias = state.linkedAlias.isNotEmpty(),
                        passwordState = state.passwordState,
                        totpUiState = state.totpUiState,
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
                        },
                        onGoToAliasClick = {
                            state.linkedAlias.map {
                                onNavigate(
                                    ItemDetailNavigation.OnViewItem(
                                        shareId = it.shareId,
                                        itemId = it.itemId,
                                    )
                                )
                            }
                        }
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    onConfirm = {
                        shouldShowDeleteItemDialog = false
                        viewModel.onPermanentlyDelete(
                            state.itemUiModel.shareId,
                            state.itemUiModel.id,
                            state.itemUiModel.itemType
                        )
                    },
                    onDismiss = { shouldShowDeleteItemDialog = false }
                )
            }
        }
    }
}

