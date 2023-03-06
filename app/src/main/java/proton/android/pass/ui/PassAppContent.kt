package proton.android.pass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHostState
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.featurehome.impl.HomeItemTypeSelection
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featuresettings.impl.Settings
import proton.android.pass.featuretrash.impl.Trash
import proton.android.pass.featurevault.impl.VaultList
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.presentation.navigation.CoreNavigation
import proton.android.pass.presentation.navigation.drawer.NavDrawerNavigation
import proton.android.pass.presentation.navigation.drawer.NavigationDrawerSection
import proton.android.pass.presentation.navigation.drawer.SelectedItemTypes
import proton.android.pass.presentation.navigation.drawer.SelectedVaults
import proton.android.pass.ui.internal.InternalDrawerState
import proton.android.pass.ui.internal.InternalDrawerValue
import proton.android.pass.ui.internal.rememberInternalDrawerState

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PassAppContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    internalDrawerState: InternalDrawerState = rememberInternalDrawerState(InternalDrawerValue.Closed),
    coreNavigation: CoreNavigation,
    onDrawerSectionChanged: (NavigationDrawerSection) -> Unit,
    onSnackbarMessageDelivered: () -> Unit,
    finishActivity: () -> Unit
) {
    val appNavigator = rememberAppNavigator()
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)
    var homeItemTypeSelection: HomeItemTypeSelection by remember {
        mutableStateOf(HomeItemTypeSelection.AllItems)
    }
    var homeVaultSelection: HomeVaultSelection by remember { mutableStateOf(HomeVaultSelection.AllVaults) }
    CurrentRouteDrawerSelection(
        appNavigator.currentDestination?.route,
        homeVaultSelection,
        homeItemTypeSelection,
        onDrawerSectionChanged
    )
    val navDrawerNavigation = NavDrawerNavigation(
        onNavHome = { selectedItemType, selectedVault ->
            homeItemTypeSelection = when (selectedItemType) {
                SelectedItemTypes.AllItems -> HomeItemTypeSelection.AllItems
                SelectedItemTypes.Logins -> HomeItemTypeSelection.Logins
                SelectedItemTypes.Aliases -> HomeItemTypeSelection.Aliases
                SelectedItemTypes.Notes -> HomeItemTypeSelection.Notes
            }
            homeVaultSelection = when (selectedVault) {
                SelectedVaults.AllVaults -> HomeVaultSelection.AllVaults
                is SelectedVaults.Vault -> HomeVaultSelection.Vault(selectedVault.shareId)
            }
            appNavigator.navigate(destination = Home)
        },
        onNavSettings = { appNavigator.navigate(Settings) },
        onNavTrash = { appNavigator.navigate(Trash) },
        onBugReport = { coreNavigation.onReport() },
        onInternalDrawerClick = { coroutineScope.launch { internalDrawerState.open() } }
    )

    SnackBarLaunchedEffect(
        appUiState.snackbarMessage.value(),
        passSnackbarHostState,
        onSnackbarMessageDelivered
    )
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { contentPadding ->
        InternalDrawer(
            drawerState = internalDrawerState,
            onOpenVault = {
                coroutineScope.launch {
                    internalDrawerState.close()
                }
                appNavigator.navigate(VaultList)
            },
            content = {
                Column(modifier = Modifier.padding(contentPadding)) {
                    ModalBottomSheetLayout(appNavigator.bottomSheetNavigator) {
                        PassNavHost(
                            modifier = Modifier.weight(1f),
                            drawerUiState = appUiState.drawerUiState,
                            appNavigator = appNavigator,
                            homeItemTypeSelection = homeItemTypeSelection,
                            homeVaultSelection = homeVaultSelection,
                            navDrawerNavigation = navDrawerNavigation,
                            coreNavigation = coreNavigation,
                            finishActivity = finishActivity
                        )
                    }

                    AnimatedVisibility(visible = appUiState.networkStatus == NetworkStatus.Offline) {
                        OfflineIndicator()
                    }
                }
            }
        )
    }
}

@Composable
private fun CurrentRouteDrawerSelection(
    currentRoute: String?,
    homeVaultSelection: HomeVaultSelection,
    homeItemTypeSelection: HomeItemTypeSelection,
    onDrawerSectionChanged: (NavigationDrawerSection) -> Unit
) {
    when (currentRoute) {
        Home.route -> {
            val shareId = when (homeVaultSelection) {
                HomeVaultSelection.AllVaults -> null
                is HomeVaultSelection.Vault -> homeVaultSelection.shareId
            }
            when (homeItemTypeSelection) {
                HomeItemTypeSelection.AllItems -> onDrawerSectionChanged(
                    NavigationDrawerSection.AllItems(shareId)
                )
                HomeItemTypeSelection.Logins -> onDrawerSectionChanged(
                    NavigationDrawerSection.Logins(shareId)
                )
                HomeItemTypeSelection.Aliases -> onDrawerSectionChanged(
                    NavigationDrawerSection.Aliases(shareId)
                )
                HomeItemTypeSelection.Notes -> onDrawerSectionChanged(
                    NavigationDrawerSection.Notes(shareId)
                )
            }
        }
        Settings.route -> onDrawerSectionChanged(NavigationDrawerSection.Settings)
        Trash.route -> onDrawerSectionChanged(NavigationDrawerSection.Trash)
        else -> {}
    }
}

@Composable
private fun SnackBarLaunchedEffect(
    snackBarMessage: SnackbarMessage?,
    passSnackBarHostState: PassSnackbarHostState,
    onSnackBarMessageDelivered: () -> Unit
) {
    snackBarMessage ?: return
    val snackBarMessageLocale = stringResource(id = snackBarMessage.id)
    LaunchedEffect(snackBarMessage) {
        passSnackBarHostState.showSnackbar(
            snackBarMessage.type,
            snackBarMessageLocale
        )
        onSnackBarMessageDelivered()
    }
}
