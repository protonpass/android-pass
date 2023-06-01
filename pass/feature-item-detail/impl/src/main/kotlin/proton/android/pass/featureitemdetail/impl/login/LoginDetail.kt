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
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.TopBarOptions
import proton.android.pass.featureitemdetail.impl.login.LoginDetailBottomSheetType.WebsiteOptions
import proton.android.pass.featureitemdetail.impl.login.bottomsheet.WebsiteOptionsBottomSheetContents
import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldEvent
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemState

@OptIn(
    ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Suppress("ComplexMethod")
fun LoginDetail(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    canLoadExternalImages: Boolean,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: LoginDetailViewModel = hiltViewModel()
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
                                canMigrate = state.canMigrate,
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
                                },
                                icon = {
                                    LoginIcon(
                                        text = state.itemUiModel.contents.title,
                                        content = state.itemUiModel.contents as ItemContents.Login,
                                        canLoadExternalImages = canLoadExternalImages
                                    )
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
                            onEditClick = { onNavigate(ItemDetailNavigation.OnEdit(state.itemUiModel)) },
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
                        totpUiState = state.totpUiState,
                        moreInfoUiState = moreInfoUiState,
                        canLoadExternalImages = canLoadExternalImages,
                        customFields = state.customFields,
                        onEvent = {
                            when (it) {
                                LoginDetailEvent.OnCopyPasswordClick -> {
                                    viewModel.copyPasswordToClipboard()
                                }
                                is LoginDetailEvent.OnCopyTotpClick -> {
                                    viewModel.copyTotpCodeToClipboard(it.totpCode)
                                }
                                is LoginDetailEvent.OnCustomFieldEvent -> {
                                    when (val event = it.event) {
                                        is CustomFieldEvent.CopyValue -> {
                                            viewModel.copyCustomFieldValue(event.index)
                                        }
                                        is CustomFieldEvent.ToggleFieldVisibility -> {
                                            viewModel.toggleCustomFieldVisibility(event.index)
                                        }
                                        is CustomFieldEvent.CopyValueContent -> {
                                            viewModel.copyCustomFieldContent(event.content)
                                        }
                                    }
                                }
                                LoginDetailEvent.OnGoToAliasClick -> {
                                    state.linkedAlias.map {
                                        onNavigate(
                                            ItemDetailNavigation.OnViewItem(
                                                shareId = it.shareId,
                                                itemId = it.itemId,
                                            )
                                        )
                                    }
                                }
                                LoginDetailEvent.OnTogglePasswordClick -> {
                                    viewModel.togglePassword()
                                }
                                LoginDetailEvent.OnUpgradeClick -> {
                                    onNavigate(ItemDetailNavigation.Upgrade)
                                }
                                LoginDetailEvent.OnUsernameClick -> {
                                    viewModel.copyUsernameToClipboard()
                                }
                                is LoginDetailEvent.OnWebsiteClicked -> {
                                    openWebsite(context, it.website)
                                }
                                is LoginDetailEvent.OnWebsiteLongClicked -> {
                                    selectedWebsite = it.website
                                    currentBottomSheet = WebsiteOptions
                                    scope.launch { bottomSheetState.show() }
                                }
                            }
                        }
                    )
                }
                ConfirmDeleteItemDialog(
                    isLoading = state.isLoading,
                    show = shouldShowDeleteItemDialog,
                    onConfirm = {
                        shouldShowDeleteItemDialog = false
                        viewModel.onPermanentlyDelete(state.itemUiModel)
                    },
                    onDismiss = { shouldShowDeleteItemDialog = false }
                )
            }
        }
    }
}

