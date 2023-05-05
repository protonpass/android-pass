package proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.featuresearchoptions.impl.SortingBottomsheet
import proton.android.featuresearchoptions.impl.SortingNavigation
import proton.android.featuresearchoptions.impl.sortingGraph
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.featureaccount.impl.Account
import proton.android.pass.featureaccount.impl.AccountNavigation
import proton.android.pass.featureaccount.impl.SignOutDialog
import proton.android.pass.featureaccount.impl.accountGraph
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurefeatureflags.impl.featureFlagsGraph
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.featurehome.impl.HomeNavigation
import proton.android.pass.featurehome.impl.homeGraph
import proton.android.pass.featureitemcreate.impl.alias.CreateAlias
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.alias.EditAlias
import proton.android.pass.featureitemcreate.impl.alias.createAliasGraph
import proton.android.pass.featureitemcreate.impl.alias.updateAliasGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.bottomsheetCreateItemGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.generatepassword.GeneratePasswordBottomsheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.generatepassword.generatePasswordBottomsheetGraph
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.EditLogin
import proton.android.pass.featureitemcreate.impl.login.GenerateLoginPasswordBottomsheet
import proton.android.pass.featureitemcreate.impl.login.createLoginGraph
import proton.android.pass.featureitemcreate.impl.login.generatePasswordGraph
import proton.android.pass.featureitemcreate.impl.login.updateLoginGraph
import proton.android.pass.featureitemcreate.impl.note.CreateNote
import proton.android.pass.featureitemcreate.impl.note.EditNote
import proton.android.pass.featureitemcreate.impl.note.createNoteGraph
import proton.android.pass.featureitemcreate.impl.note.updateNoteGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.featureitemdetail.impl.ViewItem
import proton.android.pass.featureitemdetail.impl.itemDetailGraph
import proton.android.pass.featuremigrate.impl.MigrateConfirmVault
import proton.android.pass.featuremigrate.impl.MigrateNavigation
import proton.android.pass.featuremigrate.impl.MigrateSelectVault
import proton.android.pass.featuremigrate.impl.migrateGraph
import proton.android.pass.featureonboarding.impl.OnBoarding
import proton.android.pass.featureonboarding.impl.onBoardingGraph
import proton.android.pass.featureprofile.impl.AppLockBottomsheet
import proton.android.pass.featureprofile.impl.FeedbackBottomsheet
import proton.android.pass.featureprofile.impl.Profile
import proton.android.pass.featureprofile.impl.ProfileNavigation
import proton.android.pass.featureprofile.impl.profileGraph
import proton.android.pass.featuresettings.impl.ClearClipboardOptions
import proton.android.pass.featuresettings.impl.ClipboardSettings
import proton.android.pass.featuresettings.impl.LogView
import proton.android.pass.featuresettings.impl.SelectPrimaryVault
import proton.android.pass.featuresettings.impl.Settings
import proton.android.pass.featuresettings.impl.ThemeSelector
import proton.android.pass.featuresettings.impl.settingsGraph
import proton.android.pass.featurevault.impl.bottomsheet.CreateVaultBottomSheet
import proton.android.pass.featurevault.impl.bottomsheet.EditVaultBottomSheet
import proton.android.pass.featurevault.impl.delete.DeleteVaultDialog
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.AppNavigation
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList", "LongMethod", "ComplexMethod")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit,
    onAuthPerformed: () -> Unit
) {
    homeGraph(
        onNavigateEvent = {
            when (it) {
                is HomeNavigation.AddItem -> {
                    val (destination, route) = when (it.itemTypeUiState) {
                        ItemTypeUiState.Unknown ->
                            CreateItemBottomsheet to CreateItemBottomsheet.createNavRoute(it.shareId)

                        ItemTypeUiState.Login -> CreateLogin to CreateLogin.createNavRoute(it.shareId)
                        ItemTypeUiState.Note -> CreateNote to CreateNote.createNavRoute(it.shareId)
                        ItemTypeUiState.Alias -> CreateAlias to CreateAlias.createNavRoute(it.shareId)
                        ItemTypeUiState.Password -> GeneratePasswordBottomsheet to null
                    }

                    appNavigator.navigate(destination, route)
                }

                HomeNavigation.Auth -> {
                    appNavigator.navigate(Auth)
                }

                HomeNavigation.CreateVault -> {
                    appNavigator.navigate(CreateVaultBottomSheet)
                }

                is HomeNavigation.DeleteVault -> {
                    appNavigator.navigate(
                        destination = DeleteVaultDialog,
                        route = DeleteVaultDialog.createNavRoute(it.shareId),
                        backDestination = Home
                    )
                }

                is HomeNavigation.EditAlias -> {
                    appNavigator.navigate(
                        EditAlias,
                        EditAlias.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.EditLogin -> {
                    appNavigator.navigate(
                        EditLogin,
                        EditLogin.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.EditNote -> {
                    appNavigator.navigate(
                        EditNote,
                        EditNote.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.EditVault -> {
                    appNavigator.navigate(
                        EditVaultBottomSheet,
                        EditVaultBottomSheet.createNavRoute(it.shareId.toOption())
                    )
                }

                is HomeNavigation.MigrateVault -> {
                    appNavigator.navigate(
                        MigrateSelectVault,
                        MigrateSelectVault.createNavRouteForMigrateAll(it.shareId)
                    )
                }

                is HomeNavigation.ItemDetail -> {
                    appNavigator.navigate(
                        ViewItem,
                        ViewItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                HomeNavigation.OnBoarding -> {
                    appNavigator.navigate(OnBoarding)
                }

                HomeNavigation.Profile -> {
                    appNavigator.navigate(Profile)
                }

                is HomeNavigation.SortingBottomsheet -> {
                    appNavigator.navigate(
                        SortingBottomsheet,
                        SortingBottomsheet.createNavRoute(it.searchSortingType)
                    )
                }
            }
        }
    )
    sortingGraph(
        onNavigateEvent = {
            when (it) {
                is SortingNavigation.SelectSorting -> dismissBottomSheet {}
            }
        }
    )
    bottomsheetCreateItemGraph(
        onCreateLogin = { shareId ->
            appNavigator.navigate(
                CreateLogin,
                CreateLogin.createNavRoute(shareId)
            )
        },
        onCreateAlias = { shareId ->
            appNavigator.navigate(
                CreateAlias,
                CreateAlias.createNavRoute(shareId)
            )
        },
        onCreateNote = { shareId ->
            appNavigator.navigate(
                CreateNote,
                CreateNote.createNavRoute(shareId)
            )
        },
        onCreatePassword = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(Profile) -> Profile
                appNavigator.hasDestinationInStack(Home) -> Home
                else -> null
            }
            appNavigator.navigate(
                destination = GeneratePasswordBottomsheet,
                backDestination = backDestination
            )
        }
    )
    vaultGraph(
        dismissBottomSheet = { dismissBottomSheet({}) },
        onClose = { appNavigator.onBackClick() }
    )
    generatePasswordBottomsheetGraph(
        onDismiss = { appNavigator.onBackClick() }
    )
    accountGraph(
        onNavigate = {
            when (it) {
                AccountNavigation.Back -> appNavigator.onBackClick()
                AccountNavigation.ConfirmSignOut -> onNavigate(AppNavigation.SignOut())
                AccountNavigation.DismissDialog -> appNavigator.onBackClick()
                AccountNavigation.SignOut -> appNavigator.navigate(SignOutDialog)
                AccountNavigation.Subscription -> onNavigate(AppNavigation.Subscription)
                AccountNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
    )
    profileGraph(
        dismissBottomSheet = { dismissBottomSheet({}) },
        onNavigateEvent = {
            when (it) {
                ProfileNavigation.Account -> appNavigator.navigate(Account)
                ProfileNavigation.Settings -> appNavigator.navigate(Settings)
                ProfileNavigation.List -> appNavigator.navigate(Home)
                ProfileNavigation.CreateItem -> appNavigator.navigate(CreateItemBottomsheet)
                ProfileNavigation.Feedback -> appNavigator.navigate(FeedbackBottomsheet)
                ProfileNavigation.AppLock -> appNavigator.navigate(AppLockBottomsheet)
            }
        }
    )
    settingsGraph(
        onSelectThemeClick = { appNavigator.navigate(ThemeSelector) },
        onUpClick = { appNavigator.onBackClick() },
        dismissBottomSheet = { dismissBottomSheet({}) },
        onViewLogsClick = { appNavigator.navigate(LogView) },
        onClipboardClick = { appNavigator.navigate(ClipboardSettings) },
        onClearClipboardSettingClick = { appNavigator.navigate(ClearClipboardOptions) },
        onPrimaryVaultClick = { appNavigator.navigate(SelectPrimaryVault) }
    )
    createLoginGraph(
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onClose = { appNavigator.onBackClick() },
        onSuccess = { appNavigator.onBackClick() },
        onScanTotp = { appNavigator.navigate(CameraTotp) },
        onCreateAlias = { shareId, title ->
            appNavigator.navigate(
                destination = CreateAliasBottomSheet,
                route = CreateAliasBottomSheet.createNavRoute(shareId, title),
                backDestination = CreateLogin
            )
        },
        onGeneratePasswordClick = {
            appNavigator.navigate(GenerateLoginPasswordBottomsheet)
        }
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
        onScanTotp = { appNavigator.navigate(CameraTotp) },
        onCreateAlias = { shareId, title ->
            appNavigator.navigate(
                destination = CreateAliasBottomSheet,
                route = CreateAliasBottomSheet.createNavRoute(shareId, title),
                backDestination = EditLogin
            )
        },
        onGeneratePasswordClick = {
            appNavigator.navigate(GenerateLoginPasswordBottomsheet)
        }
    )
    generatePasswordGraph(dismissBottomSheet = dismissBottomSheet)
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
        dismissBottomSheet = dismissBottomSheet,
        onAliasCreatedSuccess = { appNavigator.onBackClick() },
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
        onMigrateClick = { shareId: ShareId, itemId: ItemId ->
            appNavigator.navigate(
                destination = MigrateSelectVault,
                route = MigrateSelectVault.createNavRouteForMigrateItem(shareId, itemId)
            )
        },

        onBackClick = { appNavigator.onBackClick() }
    )

    migrateGraph(
        navigation = {
            when (it) {
                is MigrateNavigation.VaultSelectedForMigrateItem -> {
                    dismissBottomSheet {
                        appNavigator.navigate(
                            destination = MigrateConfirmVault,
                            route = MigrateConfirmVault.createNavRouteForMigrateItem(
                                shareId = it.sourceShareId,
                                itemId = it.itemId,
                                destShareId = it.destShareId
                            ),
                            backDestination = ViewItem
                        )
                    }
                }
                is MigrateNavigation.ItemMigrated -> {
                    dismissBottomSheet {
                        appNavigator.navigate(
                            destination = ViewItem,
                            route = ViewItem.createNavRoute(it.shareId, it.itemId),
                            backDestination = Home
                        )
                    }
                }
                MigrateNavigation.VaultMigrated -> {
                    dismissBottomSheet { appNavigator.onBackClick() }
                }
                is MigrateNavigation.VaultSelectedForMigrateAll -> {
                    dismissBottomSheet {
                        appNavigator.navigate(
                            destination = MigrateConfirmVault,
                            route = MigrateConfirmVault.createNavRouteForMigrateAll(
                                shareId = it.sourceShareId,
                                destShareId = it.destShareId
                            ),
                            backDestination = Home
                        )
                    }
                }
            }
        },
        dismissBottomSheet = dismissBottomSheet,
    )

    authGraph(
        navigation = {
            when (it) {
                AuthNavigation.Back -> { onNavigate(AppNavigation.Finish) }
                AuthNavigation.Success -> {
                    onAuthPerformed()
                    appNavigator.onBackClick()
                }
                AuthNavigation.Dismissed -> { onNavigate(AppNavigation.Finish) }
                AuthNavigation.Failed -> { appNavigator.onBackClick() }
            }
        }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.onBackClick() },
        onNavigateBack = { onNavigate(AppNavigation.Finish) }
    )
    featureFlagsGraph()
}
