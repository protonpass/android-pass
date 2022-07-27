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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.headline
import me.proton.core.compose.theme.headlineSmall
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer

@ExperimentalMaterialApi
object HomeScreen {

    @Composable
    fun view(
        modifier: Modifier = Modifier,
        onDrawerStateChanged: (Boolean) -> Unit = {},
        onSignIn: (UserId?) -> Unit = {},
        onSignOut: (UserId) -> Unit = {},
        onRemove: (UserId?) -> Unit = {},
        onSwitch: (UserId) -> Unit = {},
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
                BottomSheetContents()
            }
        ) {
            Scaffold(
                modifier = modifier.systemBarsPadding(),
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
                    TopAppBar(
                        elevation = 0.dp,
                        backgroundColor = Color.White,
                        title = {
                            val title = when (val topBarTitle = viewState.topBarTitle) {
                                is HomeViewModel.TopBarTitle.AllShares -> stringResource(id = R.string.title_all_shares)
                                is HomeViewModel.TopBarTitle.ShareName -> topBarTitle.name
                            }
                            Text(title, style = ProtonTheme.typography.headline)
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
                        viewState.items,
                        modifier = Modifier.padding(contentPadding),
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
    onDeleteItemClicked: (ItemUiModel) -> Unit,
) {
    if (itemsList.isNotEmpty()) {
        LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
            items(itemsList) { item ->
                ItemRow(item = item, onDeleteClicked = onDeleteItemClicked)
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
internal fun ItemRow(item: ItemUiModel, onDeleteClicked: (ItemUiModel) -> Unit) {
    val typeText = stringResource(
        when (item.itemType) {
            is ItemType.Login -> R.string.item_type_login
            is ItemType.Note -> R.string.item_type_note
        }
    )
    Row(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = ProtonTheme.typography.headlineSmall)
            when (val contents = item.itemType) {
                is ItemType.Login -> {
                    ItemRowContents(
                        secretValue = stringResource(
                            id = R.string.item_type_login_format, contents.username
                        ),
                        showContents = true
                    )
                    ItemRowContents(
                        secretValue = stringResource(
                            id = R.string.item_secret_login_password, "**********"
                        ),
                        showContents = false
                    )
                }
                is ItemType.Note -> {
                    ItemRowContents(
                        secretValue = stringResource(
                            id = R.string.item_type_note_format, item.name
                        ),
                        showContents = true
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.item_secret_type_message, typeText),
                style = ProtonTheme.typography.default
            )
        }
        IconButton(
            onClick = { onDeleteClicked(item) },
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_baseline_delete_24),
                contentDescription = stringResource(id = R.string.action_delete)
            )
        }
    }
}

@Composable
internal fun ItemRowContents(secretValue: String, showContents: Boolean) {
    val contents = if (showContents)
        secretValue else
        "*******"
    Text(text = contents, style = ProtonTheme.typography.default)
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

@Composable
private fun BottomSheetContents() {
    Column {
        Text(
            text = stringResource(R.string.title_new),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItem(R.drawable.ic_proton_key, R.string.action_login, onItemClick = {})
        BottomSheetItem(R.drawable.ic_proton_alias, R.string.action_alias, onItemClick = {})
        BottomSheetItem(R.drawable.ic_proton_note, R.string.action_note, onItemClick = {})
        BottomSheetItem(R.drawable.ic_proton_arrows_rotate, R.string.action_password, onItemClick = {})
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
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = stringResource(title), fontSize = 16.sp)
    }
}
// @Preview(showBackground = true)
// @Composable
// private fun Preview_Home() {
//    val secrets = listOf(
//        Secret(
//            UUID.randomUUID().toString(),
//            "user_1",
//            "address_1",
//            "Login secret",
//            SecretType.Login,
//            false,
//            SecretValue.Login("Username", "Password"),
//            listOf("me.proton.android.pass")
//        ),
//        Secret(
//            UUID.randomUUID().toString(),
//            "user_1",
//            "address_1",
//            "Full name secret",
//            SecretType.FullName,
//            false,
//            SecretValue.Single("Jorge Martín Espinosa"),
//            listOf("me.proton.android.pass")
//        )
//    )
//    Home(secrets = secrets, onDeleteSecretClicked = {})
// }
