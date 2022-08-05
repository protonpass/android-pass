package me.proton.android.pass.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer

internal typealias OnItemClick = (ShareId, ItemId) -> Unit

interface HomeScreenNavigation {
    val toCreateItem: (ShareId) -> Unit
    val toItemDetail: (ShareId, ItemId) -> Unit
    val toEditItem: (ShareId, ItemId) -> Unit
}

@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDrawerStateChanged: (Boolean) -> Unit = {},
    onSignIn: (UserId?) -> Unit = {},
    onSignOut: (UserId) -> Unit = {},
    onRemove: (UserId?) -> Unit = {},
    onSwitch: (UserId) -> Unit = {},
    navigation: HomeScreenNavigation,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        requestAutofillAccessIfNeeded(context = context)
    }

    val homeScaffoldState = rememberHomeScaffoldState()
    val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
        isOpen && !isAnimationRunning || isClosed && isAnimationRunning
    }
    LaunchedEffect(isDrawerOpen) {
        onDrawerStateChanged(isDrawerOpen)
    }
    val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled

    val viewState by rememberFlowWithLifecycle(flow = homeViewModel.viewState)
        .collectAsState(initial = homeViewModel.initialViewState)

    val viewEvent = homeViewModel.viewEvent(
        navigateToSigningOut = { onRemove(null) },
    )
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                scope = coroutineScope,
                state = bottomSheetState,
                navigation = navigation,
                shareId = viewState.selectedShare,
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            scaffoldState = homeScaffoldState.scaffoldState,
            drawerContent = {
                NavigationDrawer(
                    drawerState = homeScaffoldState.scaffoldState.drawerState,
                    viewState = viewState.navigationDrawerViewState,
                    viewEvent = viewEvent.navigationDrawerViewEvent,
                    shares = viewState.shares,
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    onRemove = onRemove,
                    onSignIn = onSignIn,
                    onSignOut = onSignOut,
                    onSwitch = onSwitch,
                )
            },
            drawerGesturesEnabled = drawerGesturesEnabled,
            topBar = {
                HomeTopBar(
                    viewState = viewState,
                    homeScaffoldState = homeScaffoldState,
                    bottomSheetState = bottomSheetState,
                    coroutineScope = coroutineScope
                )
            }
        ) { contentPadding ->
            Box {
                val itemToDelete = remember { mutableStateOf<ItemUiModel?>(null) }
                Home(
                    items = viewState.items,
                    modifier = Modifier.padding(contentPadding),
                    onItemClick = { shareId, itemId -> navigation.toItemDetail(shareId, itemId) },
                    onEditItemClick = { shareId, itemId -> navigation.toEditItem(shareId, itemId) },
                    onDeleteItemClicked = { item -> itemToDelete.value = item }
                )
                ConfirmItemDeletionDialog(
                    itemState = itemToDelete,
                    onConfirm = { homeViewModel.deleteItem(it) }
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun HomeTopBar(
    viewState: HomeViewModel.ViewState,
    homeScaffoldState: HomeScaffoldState,
    bottomSheetState: ModalBottomSheetState,
    coroutineScope: CoroutineScope,
) {
    val (isSearchMode, setIsSearchMode) = remember { mutableStateOf(false) }

    ProtonTopAppBar(
        title = {
            if (isSearchMode) {
                HomeSearchBar()
            } else {
                HomeTopBarTitle(viewState)
            }
        },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable(onClick = {
                    val drawerState = homeScaffoldState.scaffoldState.drawerState
                    coroutineScope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                }),
                contentDescription = null,
            )
        },
        actions = {
            IconButton(onClick = {
                setIsSearchMode(true)
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    if (bottomSheetState.isVisible) {
                        bottomSheetState.hide()
                    } else {
                        bottomSheetState.show()
                    }
                }
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_plus),
                    contentDescription = stringResource(R.string.action_create),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Composable
private fun HomeSearchBar() {
    Text(text = "TODO: Search :)")
}

@ExperimentalMaterialApi
@Composable
private fun HomeTopBarTitle(viewState: HomeViewModel.ViewState) {
    val title = when (val topBarTitle = viewState.topBarTitle) {
        is HomeViewModel.TopBarTitle.AllShares -> stringResource(id = R.string.title_all_shares)
        is HomeViewModel.TopBarTitle.ShareName -> topBarTitle.name
    }
    TopBarTitleView(title = title)
}

@Composable
private fun ConfirmItemDeletionDialog(
    itemState: MutableState<ItemUiModel?>,
    onConfirm: (ItemUiModel?) -> Unit,
) {
    val item = itemState.value
    if (item != null) {
        AlertDialog(
            onDismissRequest = { itemState.value = null },
            title = { Text(stringResource(R.string.alert_confirm_secret_deletion_title)) },
            text = {
                Text(stringResource(R.string.alert_confirm_secret_deletion_message, item.name))
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(item)
                    itemState.value = null
                }) {
                    Text(text = stringResource(id = R.string.presentation_alert_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemState.value = null }) {
                    Text(text = stringResource(id = R.string.presentation_alert_cancel))
                }
            }
        )
    }
}

@Composable
internal fun Home(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: OnItemClick,
    onEditItemClick: OnItemClick,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
) {
    if (items.isNotEmpty()) {
        ItemsList(
            items = items,
            modifier = modifier,
            onItemClick = onItemClick,
            onEditItemClick = onEditItemClick,
            onDeleteItemClicked = onDeleteItemClicked
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "You don't have any saved credentials.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Stable
@ExperimentalMaterialApi
data class HomeScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>,
)

@Composable
@ExperimentalMaterialApi
fun rememberHomeScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true),
): HomeScaffoldState = remember {
    HomeScaffoldState(
        scaffoldState,
        drawerGesturesEnabled,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun requestAutofillAccessIfNeeded(context: Context) {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    if (!autofillManager.hasEnabledAutofillServices()) {
        LaunchedEffect(true) {
            val intent = Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}
