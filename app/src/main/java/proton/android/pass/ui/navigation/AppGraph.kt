package proton.android.pass.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.featuresearchoptions.impl.SortingBottomsheet
import proton.android.featuresearchoptions.impl.SortingNavigation
import proton.android.featuresearchoptions.impl.sortingGraph
import proton.android.pass.common.api.some
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
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasNavigation
import proton.android.pass.featureitemcreate.impl.alias.EditAlias
import proton.android.pass.featureitemcreate.impl.alias.UpdateAliasNavigation
import proton.android.pass.featureitemcreate.impl.alias.createAliasGraph
import proton.android.pass.featureitemcreate.impl.alias.updateAliasGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomSheetMode
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.bottomsheetCreateItemGraph
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.EditLogin
import proton.android.pass.featureitemcreate.impl.login.createLoginGraph
import proton.android.pass.featureitemcreate.impl.login.updateLoginGraph
import proton.android.pass.featureitemcreate.impl.note.CreateNote
import proton.android.pass.featureitemcreate.impl.note.EditNote
import proton.android.pass.featureitemcreate.impl.note.createNoteGraph
import proton.android.pass.featureitemcreate.impl.note.updateNoteGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ViewItem
import proton.android.pass.featureitemdetail.impl.itemDetailGraph
import proton.android.pass.featuremigrate.impl.MigrateConfirmVault
import proton.android.pass.featuremigrate.impl.MigrateNavigation
import proton.android.pass.featuremigrate.impl.MigrateSelectVault
import proton.android.pass.featuremigrate.impl.migrateGraph
import proton.android.pass.featureonboarding.impl.OnBoarding
import proton.android.pass.featureonboarding.impl.onBoardingGraph
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheet
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.featurepassword.impl.dialog.mode.PasswordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.WordSeparatorDialog
import proton.android.pass.featurepassword.impl.generatePasswordBottomsheetGraph
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
import proton.android.pass.featurevault.impl.VaultNavigation
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
                        ItemTypeUiState.Password ->
                            GeneratePasswordBottomsheet to GeneratePasswordBottomsheet.buildRoute(
                                mode = GeneratePasswordBottomsheetModeValue.CopyAndClose
                            )
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
        mode = CreateItemBottomSheetMode.Full,
        onNavigate = {
            when (it) {
                is CreateItemBottomsheetNavigation.CreateAlias -> {
                    appNavigator.navigate(
                        CreateAlias,
                        CreateAlias.createNavRoute(it.shareId)
                    )
                }

                is CreateItemBottomsheetNavigation.CreateLogin -> {
                    appNavigator.navigate(
                        CreateLogin,
                        CreateLogin.createNavRoute(it.shareId)
                    )
                }

                is CreateItemBottomsheetNavigation.CreateNote -> {
                    appNavigator.navigate(
                        CreateNote,
                        CreateNote.createNavRoute(it.shareId)
                    )
                }

                CreateItemBottomsheetNavigation.CreatePassword -> {
                    val backDestination = when {
                        appNavigator.hasDestinationInStack(Profile) -> Profile
                        appNavigator.hasDestinationInStack(Home) -> Home
                        else -> null
                    }
                    appNavigator.navigate(
                        destination = GeneratePasswordBottomsheet,
                        route = GeneratePasswordBottomsheet.buildRoute(
                            mode = GeneratePasswordBottomsheetModeValue.CopyAndClose
                        ),
                        backDestination = backDestination
                    )
                }
            }
        },
    )
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.onBackClick()
                VaultNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
    )
    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.onBackClick()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {
                    appNavigator.onBackClick()
                }
                GeneratePasswordNavigation.OnSelectWordSeparator -> appNavigator.navigate(
                    destination = WordSeparatorDialog
                )
                GeneratePasswordNavigation.OnSelectPasswordMode -> appNavigator.navigate(
                    destination = PasswordModeDialog
                )
            }
        }
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
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> appNavigator.onBackClick()
                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(it.shareId, it.showUpgrade, it.title),
                    backDestination = CreateLogin
                )

                BaseLoginNavigation.GeneratePassword ->
                    appNavigator.navigate(
                        destination = GeneratePasswordBottomsheet,
                        route = GeneratePasswordBottomsheet.buildRoute(
                            mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                        )
                    )

                is BaseLoginNavigation.LoginCreated -> appNavigator.onBackClick()
                is BaseLoginNavigation.LoginUpdated -> {}
                BaseLoginNavigation.ScanTotp -> appNavigator.navigate(CameraTotp)
                BaseLoginNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
    )
    updateLoginGraph(
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> appNavigator.onBackClick()
                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(
                        it.shareId,
                        it.showUpgrade,
                        it.title
                    ),
                    backDestination = CreateLogin
                )

                BaseLoginNavigation.GeneratePassword ->
                    appNavigator.navigate(
                        destination = GeneratePasswordBottomsheet,
                        route = GeneratePasswordBottomsheet.buildRoute(
                            mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                        )
                    )

                is BaseLoginNavigation.LoginCreated -> {}
                is BaseLoginNavigation.LoginUpdated ->
                    appNavigator.navigate(
                        destination = ViewItem,
                        route = ViewItem.createNavRoute(it.shareId, it.itemId),
                        backDestination = Home
                    )

                BaseLoginNavigation.ScanTotp -> appNavigator.navigate(CameraTotp)
                BaseLoginNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
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
        onNavigate = {
            when (it) {
                CreateAliasNavigation.Close -> appNavigator.onBackClick()
                is CreateAliasNavigation.CreatedFromBottomsheet -> {
                    dismissBottomSheet {
                        appNavigator.onBackClick()
                    }
                }

                is CreateAliasNavigation.Created -> {
                    appNavigator.onBackClick()
                }

                CreateAliasNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        },
    )
    updateAliasGraph(
        onNavigate = {
            when (it) {
                UpdateAliasNavigation.Close -> appNavigator.onBackClick()
                is UpdateAliasNavigation.Updated -> appNavigator.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(it.shareId, it.itemId),
                    backDestination = Home
                )

                UpdateAliasNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
    )
    itemDetailGraph(
        onNavigate = {
            when (it) {
                ItemDetailNavigation.Back -> {
                    appNavigator.onBackClick()
                }

                is ItemDetailNavigation.OnCreateLoginFromAlias -> {
                    appNavigator.navigate(
                        destination = CreateLogin,
                        route = CreateLogin.createNavRoute(username = it.alias.some()),
                        backDestination = Home
                    )
                }

                is ItemDetailNavigation.OnEdit -> {
                    val destination = when (it.itemType) {
                        is ItemType.Login -> EditLogin
                        is ItemType.Note -> EditNote
                        is ItemType.Alias -> EditAlias
                        is ItemType.Password -> null // Edit password does not exist yet
                    }
                    val route = when (it.itemType) {
                        is ItemType.Login -> EditLogin.createNavRoute(it.shareId, it.itemId)
                        is ItemType.Note -> EditNote.createNavRoute(it.shareId, it.itemId)
                        is ItemType.Alias -> EditAlias.createNavRoute(it.shareId, it.itemId)
                        is ItemType.Password -> null // Edit password does not exist yet
                    }

                    if (destination != null && route != null) {
                        appNavigator.navigate(destination, route)
                    }
                }

                is ItemDetailNavigation.OnMigrate -> {
                    appNavigator.navigate(
                        destination = MigrateSelectVault,
                        route = MigrateSelectVault.createNavRouteForMigrateItem(
                            shareId = it.shareId,
                            itemId = it.itemId
                        )
                    )
                }

                is ItemDetailNavigation.OnViewItem -> {
                    appNavigator.navigate(
                        destination = ViewItem,
                        route = ViewItem.createNavRoute(it.shareId, it.itemId)
                    )
                }
            }
        }
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
                AuthNavigation.Back -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Success -> {
                    onAuthPerformed()
                    appNavigator.onBackClick()
                }

                AuthNavigation.Dismissed -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Failed -> appNavigator.onBackClick()
            }
        }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.onBackClick() },
        onNavigateBack = { onNavigate(AppNavigation.Finish) }
    )
    featureFlagsGraph()
}
