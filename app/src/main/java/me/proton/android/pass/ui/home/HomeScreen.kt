package me.proton.android.pass.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
import android.view.autofill.AutofillManager
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
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
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer

internal typealias OnItemClick = (Pair<ShareId, ItemId>) -> Unit

interface HomeScreenNavigation {
    val toCreateItem: (ShareId) -> Unit
    val toItemDetail: (Pair<ShareId, ItemId>) -> Unit
}

@ExperimentalMaterialApi
object HomeScreen {

    @Composable
    fun View(
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
                    ProtonTopAppBar(
                        title = {
                            val title = when (val topBarTitle = viewState.topBarTitle) {
                                is HomeViewModel.TopBarTitle.AllShares -> stringResource(id = R.string.title_all_shares)
                                is HomeViewModel.TopBarTitle.ShareName -> topBarTitle.name
                            }
                            TopBarTitleView(title = title)
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
                            IconButton(onClick = {}) {
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
                },
            ) { contentPadding ->
                Box {
                    val itemToDelete = remember { mutableStateOf<ItemUiModel?>(null) }
                    Home(
                        itemsList = viewState.items,
                        modifier = Modifier.padding(contentPadding),
                        onItemClick = { navigation.toItemDetail(it) },
                        onDeleteItemClicked = { item ->
                            itemToDelete.value = item
                        }
                    )
                    ConfirmSecretDeletionDialog(
                        itemState = itemToDelete,
                        onConfirm = { homeViewModel.deleteItem(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmSecretDeletionDialog(
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
    itemsList: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: OnItemClick,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
) {
    if (itemsList.isNotEmpty()) {
        LazyColumn(modifier = modifier) {
            items(itemsList) { item ->
                ItemRow(item = item, onItemClicked = onItemClick, onDeleteClicked = onDeleteItemClicked)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "You don't have any saved credentials.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
internal fun ItemRow(
    item: ItemUiModel,
    onItemClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    when (val itemType = item.itemType) {
        is ItemType.Login -> LoginRow(item, itemType, onItemClicked, onDeleteClicked)
        is ItemType.Note -> NoteRow(item, itemType, onItemClicked, onDeleteClicked)
    }
}

@Composable
internal fun LoginRow(
    item: ItemUiModel,
    itemType: ItemType.Login,
    onItemClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    ItemRow(
        icon = R.drawable.ic_proton_key,
        title = item.name,
        subtitle = itemType.username,
        onItemClicked = { onItemClicked(Pair(item.shareId, item.id)) },
        onDeleteClicked = {
            onDeleteClicked(item)
        }
    )
}

@Composable
internal fun NoteRow(
    item: ItemUiModel,
    itemType: ItemType.Note,
    onItemClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    ItemRow(
        icon = R.drawable.ic_proton_note,
        title = item.name,
        subtitle = itemType.text.take(10),
        onItemClicked = { onItemClicked(Pair(item.shareId, item.id)) },
        onDeleteClicked = {
            onDeleteClicked(item)
        }
    )
}

@Composable
internal fun ItemRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    onItemClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clickable { onItemClicked() }
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.padding(start = 20.dp)
            )
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.then(Modifier.size(24.dp))
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                        contentDescription = stringResource(id = R.string.action_delete),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties()
                ) {
                    DropdownMenuItem(onClick = {
                        /* TODO: onEditClicked */
                        expanded = false
                    }) {
                        Text(text = stringResource(id = R.string.action_edit))
                    }
                    DropdownMenuItem(onClick = {
                        onDeleteClicked()
                        expanded = false
                    }) {
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                }
            }
        }
        Row(modifier = Modifier.padding(start = 44.dp, end = 20.dp)) {
            Text(
                text = subtitle,
                color = ProtonTheme.colors.textWeak,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
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

@ExperimentalMaterialApi
@Composable
private fun BottomSheetContents(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    shareId: ShareId?,
    navigation: HomeScreenNavigation
) {
    Column {
        Text(
            text = stringResource(R.string.title_new),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItem(R.drawable.ic_proton_key, R.string.action_login, onItemClick = {
            scope.launch {
                state.hide()
                if (shareId != null) {
                    navigation.toCreateItem(shareId)
                } else {
                    // TODO: Show Snackbar saying to select one?
                }
            }
        })
        BottomSheetItem(R.drawable.ic_proton_alias, R.string.action_alias, onItemClick = {
            scope.launch { state.hide() }
        })
        BottomSheetItem(R.drawable.ic_proton_note, R.string.action_note, onItemClick = {
            scope.launch { state.hide() }
        })
        BottomSheetItem(R.drawable.ic_proton_arrows_rotate, R.string.action_password, onItemClick = {
            scope.launch { state.hide() }
        })
    }
}

@Composable
private fun BottomSheetItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onItemClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick() })
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(title))
        Text(
            text = stringResource(title),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp),
            fontWeight = FontWeight.W400,
            color = ProtonTheme.colors.textNorm,
        )
    }
}
