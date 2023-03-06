package proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurecreateitem.impl.CreateAlias
import proton.android.pass.featurecreateitem.impl.CreateLogin
import proton.android.pass.featurecreateitem.impl.CreateNote
import proton.android.pass.featurecreateitem.impl.EditAlias
import proton.android.pass.featurecreateitem.impl.EditLogin
import proton.android.pass.featurecreateitem.impl.EditNote
import proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import proton.android.pass.featurecreateitem.impl.createAliasGraph
import proton.android.pass.featurecreateitem.impl.createLoginGraph
import proton.android.pass.featurecreateitem.impl.createNoteGraph
import proton.android.pass.featurecreateitem.impl.totp.CameraTotp
import proton.android.pass.featurecreateitem.impl.totp.PhotoPickerTotp
import proton.android.pass.featurecreateitem.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featurecreateitem.impl.totp.createTotpGraph
import proton.android.pass.featurecreateitem.impl.updateAliasGraph
import proton.android.pass.featurecreateitem.impl.updateLoginGraph
import proton.android.pass.featurecreateitem.impl.updateNoteGraph
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.featurehome.impl.HomeItemTypeSelection
import proton.android.pass.featurehome.impl.HomeScreenNavigation
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featurehome.impl.homeGraph
import proton.android.pass.featureitemdetail.impl.ViewItem
import proton.android.pass.featureitemdetail.impl.itemDetailGraph
import proton.android.pass.featureonboarding.impl.OnBoarding
import proton.android.pass.featureonboarding.impl.onBoardingGraph
import proton.android.pass.featureprofile.impl.Profile
import proton.android.pass.featureprofile.impl.profileGraph
import proton.android.pass.featuresettings.impl.settingsGraph
import proton.android.pass.featurevault.impl.CreateVault
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.trash.trashGraph
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList", "LongMethod")
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
    trashGraph(
        navigationDrawer = navigationDrawer,
        onDrawerIconClick = onDrawerIconClick
    )
    profileGraph(
        onListClick = { appNavigator.navigate(Home) }
    )
    settingsGraph(
        navigationDrawer = navigationDrawer,
        onDrawerIconClick = onDrawerIconClick
    )
    createLoginGraph(
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onClose = { appNavigator.onBackClick() },
        onSuccess = { appNavigator.onBackClick() },
        onScanTotp = { appNavigator.navigate(CameraTotp) }
    )
    updateLoginGraph(
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onSuccess = { shareId, itemId ->
            appNavigator.navigate(
                destination = ViewItem,
                route = ViewItem.createNavRoute(shareId, itemId),
                backDestination = Home
            )
        },
        onUpClick = { appNavigator.onBackClick() },
        onScanTotp = { appNavigator.navigate(CameraTotp) }
    )
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
    createNoteGraph(
        onNoteCreateSuccess = { appNavigator.onBackClick() },
        onBackClick = { appNavigator.onBackClick() }
    )
    updateNoteGraph(
        onNoteUpdateSuccess = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                destination = ViewItem,
                route = ViewItem.createNavRoute(shareId, itemId),
                backDestination = Home
            )
        },
        onBackClick = { appNavigator.onBackClick() }
    )
    createAliasGraph(
        onAliasCreatedSuccess = { appNavigator.onBackClick() },
        onAliasDraftCreatedSuccess = {
            appNavigator.navigateUpWithResult(RESULT_CREATED_DRAFT_ALIAS, it)
        },
        onBackClick = { appNavigator.onBackClick() }
    )
    updateAliasGraph(
        onBackClick = { appNavigator.onBackClick() },
        onAliasUpdatedSuccess = { shareId, itemId ->
            appNavigator.navigate(
                destination = ViewItem,
                route = ViewItem.createNavRoute(shareId, itemId),
                backDestination = Home
            )
        }
    )
    itemDetailGraph(
        onEditClick = { shareId: ShareId, itemId: ItemId, itemType: ItemType ->
            val destination = when (itemType) {
                is ItemType.Login -> EditLogin
                is ItemType.Note -> EditNote
                is ItemType.Alias -> EditAlias
                is ItemType.Password -> null // Edit password does not exist yet
            }
            val route = when (itemType) {
                is ItemType.Login -> EditLogin.createNavRoute(shareId, itemId)
                is ItemType.Note -> EditNote.createNavRoute(shareId, itemId)
                is ItemType.Alias -> EditAlias.createNavRoute(shareId, itemId)
                is ItemType.Password -> null // Edit password does not exist yet
            }

            if (destination != null && route != null) {
                appNavigator.navigate(destination, route)
            }
        },
        onBackClick = { appNavigator.onBackClick() }
    )
    authGraph(
        onNavigateBack = finishActivity,
        onAuthSuccessful = { appNavigator.onBackClick() },
        onAuthDismissed = finishActivity,
        onAuthFailed = { appNavigator.onBackClick() }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.onBackClick() },
        onNavigateBack = finishActivity
    )
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
        toProfile = { appNavigator.navigate(Profile) },
        toOnBoarding = { appNavigator.navigate(OnBoarding) },
    )
