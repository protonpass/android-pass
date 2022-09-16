package me.proton.android.pass.ui.trash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.android.pass.ui.shared.ConfirmSignOutDialog
import me.proton.android.pass.ui.shared.ItemAction
import me.proton.android.pass.ui.shared.ItemsList
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer

@ExperimentalMaterialApi
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    navDrawerNavigation: NavDrawerNavigation,
    viewModel: TrashScreenViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberTrashScaffoldState()
    val isDrawerOpen = with(scaffoldState.scaffoldState.drawerState) {
        isOpen && !isAnimationRunning || isClosed && isAnimationRunning
    }
    LaunchedEffect(isDrawerOpen) {
        navDrawerNavigation.onDrawerStateChanged(isDrawerOpen)
    }
    val drawerGesturesEnabled by scaffoldState.drawerGesturesEnabled
    val navDrawerState by rememberFlowWithLifecycle(flow = viewModel.navDrawerState)
        .collectAsState(initial = viewModel.initialNavDrawerState)
    val uiState by rememberFlowWithLifecycle(flow = viewModel.uiState)
        .collectAsState(initial = TrashUiState.Loading)
    var showClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var confirmSignOutDialogState by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        scaffoldState = scaffoldState.scaffoldState,
        drawerContent = {
            NavigationDrawer(
                drawerState = scaffoldState.scaffoldState.drawerState,
                viewState = navDrawerState,
                navigation = navDrawerNavigation,
                onSignOutClick = { confirmSignOutDialogState = true }
            )
        },
        drawerGesturesEnabled = drawerGesturesEnabled,
        topBar = {
            TrashTopBar(
                coroutineScope = coroutineScope,
                scaffoldState = scaffoldState.scaffoldState,
                onClearTrashClick = { showClearTrashDialog = true }
            )
        }
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }

            when (val state = uiState) {
                is TrashUiState.Loading -> LoadingDialog()
                is TrashUiState.Content -> {
                    Trash(
                        items = state.items,
                        onRestoreClicked = { viewModel.restoreItem(it) },
                        onDeleteItemClicked = { itemToDelete = it }
                    )
                }
                is TrashUiState.Error -> Text("Something went boom: ${state.message}")
            }

            ConfirmSignOutDialog(
                state = confirmSignOutDialogState,
                onDismiss = { confirmSignOutDialogState = null },
                onConfirm = { navDrawerNavigation.onRemove(null) }
            )
            ConfirmClearTrashDialog(
                show = showClearTrashDialog,
                onDismiss = { showClearTrashDialog = false },
                onConfirm = { viewModel.clearTrash() }
            )
            ConfirmItemDeletionDialog(
                state = itemToDelete,
                onDismiss = { itemToDelete = null },
                title = R.string.alert_confirm_item_deletion_title,
                message = R.string.alert_confirm_item_deletion_message,
                onConfirm = { viewModel.deleteItem(it) }
            )
        }
    }
}

@Composable
private fun ConfirmClearTrashDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alert_confirm_clear_trash_title)) },
        text = { Text(stringResource(R.string.alert_confirm_clear_trash_message)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.presentation_alert_cancel))
            }
        }
    )
}

@ExperimentalMaterialApi
@Composable
private fun TrashTopBar(
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    onClearTrashClick: () -> Unit
) {
    ProtonTopAppBar(
        title = { TopBarTitleView(title = stringResource(id = R.string.title_trash)) },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable(onClick = {
                    val drawerState = scaffoldState.drawerState
                    coroutineScope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                }),
                contentDescription = null
            )
        },
        actions = {
            IconButton(onClick = { onClearTrashClick() }) {
                Icon(
                    painterResource(R.drawable.ic_proton_trash),
                    contentDescription = stringResource(R.string.action_empty_trash),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Composable
internal fun Trash(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onRestoreClicked: (ItemUiModel) -> Unit,
    onDeleteItemClicked: (ItemUiModel) -> Unit
) {
    if (items.isNotEmpty()) {
        ItemsList(
            items = items,
            modifier = modifier,
            itemActions = listOf(
                ItemAction(
                    onSelect = { onRestoreClicked(it) },
                    title = R.string.action_restore,
                    icon = R.drawable.ic_proton_eraser,
                    textColor = ProtonTheme.colors.textNorm
                ),
                ItemAction(
                    onSelect = { onDeleteItemClicked(it) },
                    title = R.string.action_delete,
                    icon = R.drawable.ic_proton_trash,
                    textColor = ProtonTheme.colors.notificationError
                )
            )
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.message_no_trashed_credentials),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Stable
@ExperimentalMaterialApi
data class TrashScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>
)

@Composable
@ExperimentalMaterialApi
fun rememberTrashScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true)
): TrashScaffoldState = remember {
    TrashScaffoldState(
        scaffoldState,
        drawerGesturesEnabled
    )
}
