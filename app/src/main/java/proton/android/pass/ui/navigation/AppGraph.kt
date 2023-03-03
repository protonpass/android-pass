package proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurecreateitem.impl.CreateLogin
import proton.android.pass.featurecreateitem.impl.createLoginGraph
import proton.android.pass.featurecreateitem.impl.totp.CameraTotp
import proton.android.pass.featurecreateitem.impl.totp.PhotoPickerTotp
import proton.android.pass.featurecreateitem.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featurecreateitem.impl.totp.createTotpGraph
import proton.android.pass.featurehome.impl.HomeItemTypeSelection
import proton.android.pass.featurehome.impl.HomeScreenNavigation
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featurehome.impl.homeGraph
import proton.android.pass.featurevault.impl.CreateVault
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.create.alias.createAliasGraph
import proton.android.pass.ui.create.alias.updateAliasGraph
import proton.android.pass.ui.create.login.updateLoginGraph
import proton.android.pass.ui.create.note.createNoteGraph
import proton.android.pass.ui.create.note.updateNoteGraph
import proton.android.pass.ui.detail.itemDetailGraph
import proton.android.pass.ui.onboarding.onBoardingGraph
import proton.android.pass.ui.settings.settingsGraph
import proton.android.pass.ui.trash.trashGraph
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    onDrawerIconClick: () -> Unit,
    finishActivity: () -> Unit
) {
    homeGraph(
        navigationDrawer = navigationDrawer,
        homeScreenNavigation = createHomeScreenNavigation(appNavigator),
        onDrawerIconClick = onDrawerIconClick,
        homeItemTypeSelection = homeItemTypeSelection,
        homeVaultSelection = homeVaultSelection
    )
    trashGraph(navigationDrawer, onDrawerIconClick)
    settingsGraph(navigationDrawer, onDrawerIconClick)
    createLoginGraph(
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onClose = { appNavigator.onBackClick() },
        onSuccess = { appNavigator.onBackClick() },
        onScanTotp = { appNavigator.navigate(CameraTotp) }
    )
    updateLoginGraph(appNavigator)
    createTotpGraph(
        onUriReceived = { totp -> appNavigator.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, totp) },
        onCloseTotp = { appNavigator.onBackClick() },
        onOpenImagePicker = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateLogin) -> CreateLogin
                appNavigator.hasDestinationInStack(EditLogin) -> EditLogin
                else -> null
            }
            appNavigator.navigate(
                destination = PhotoPickerTotp,
                backDestination = backDestination
            )
        }
    )
    createNoteGraph(appNavigator)
    updateNoteGraph(appNavigator)
    createAliasGraph(appNavigator)
    updateAliasGraph(appNavigator)
    itemDetailGraph(appNavigator)
    authGraph(
        onNavigateBack = finishActivity,
        onAuthSuccessful = { appNavigator.onBackClick() },
        onAuthDismissed = finishActivity,
        onAuthFailed = { appNavigator.onBackClick() }
    )
    onBoardingGraph(appNavigator, finishActivity)
    vaultGraph(
        onNavigateToCreateVault = { appNavigator.navigate(CreateVault) },
        onNavigateUp = { appNavigator.onBackClick() }
    )
}

private fun createHomeScreenNavigation(appNavigator: AppNavigator): HomeScreenNavigation =
    HomeScreenNavigation(
        toCreateLogin = { shareId ->
            appNavigator.navigate(
                CreateLogin,
                CreateLogin.createNavRoute(shareId)
            )
        },
        toEditLogin = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                EditLogin,
                EditLogin.createNavRoute(shareId, itemId)
            )
        },
        toCreateNote = { shareId ->
            appNavigator.navigate(
                CreateNote,
                CreateNote.createNavRoute(shareId)
            )
        },
        toEditNote = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                EditNote,
                EditNote.createNavRoute(shareId, itemId)
            )
        },
        toCreateAlias = { shareId ->
            appNavigator.navigate(
                CreateAlias,
                CreateAlias.createNavRoute(shareId)
            )
        },
        toEditAlias = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                EditAlias,
                EditAlias.createNavRoute(shareId, itemId)
            )
        },
        toItemDetail = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                ViewItem,
                ViewItem.createNavRoute(shareId, itemId)
            )
        },
        toAuth = { appNavigator.navigate(Auth) },
        toOnBoarding = { appNavigator.navigate(OnBoarding) },
    )
