/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.ui.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.domain.ItemContents
import proton.android.pass.featureaccount.impl.Account
import proton.android.pass.featureaccount.impl.AccountNavigation
import proton.android.pass.featureaccount.impl.SignOutDialog
import proton.android.pass.featureaccount.impl.accountGraph
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurefeatureflags.impl.FeatureFlagRoute
import proton.android.pass.featurefeatureflags.impl.featureFlagsGraph
import proton.android.pass.featurehome.impl.HOME_ENABLE_BULK_ACTIONS_KEY
import proton.android.pass.featurehome.impl.HOME_GO_TO_VAULT_KEY
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
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.AddCustomFieldBottomSheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCard
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.EditCreditCard
import proton.android.pass.featureitemcreate.impl.creditcard.UpdateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.createCreditCardGraph
import proton.android.pass.featureitemcreate.impl.creditcard.updateCreditCardGraph
import proton.android.pass.featureitemcreate.impl.dialogs.CustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.dialogs.EditCustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.CreateLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.EditLogin
import proton.android.pass.featureitemcreate.impl.login.UpdateLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.login.createUpdateLoginGraph
import proton.android.pass.featureitemcreate.impl.note.CreateNote
import proton.android.pass.featureitemcreate.impl.note.CreateNoteNavigation
import proton.android.pass.featureitemcreate.impl.note.EditNote
import proton.android.pass.featureitemcreate.impl.note.UpdateNoteNavigation
import proton.android.pass.featureitemcreate.impl.note.createNoteGraph
import proton.android.pass.featureitemcreate.impl.note.updateNoteGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemdetail.impl.ItemDetailCannotPerformAction
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ViewItem
import proton.android.pass.featureitemdetail.impl.itemDetailGraph
import proton.android.pass.featuremigrate.impl.MigrateConfirmVault
import proton.android.pass.featuremigrate.impl.MigrateNavigation
import proton.android.pass.featuremigrate.impl.MigrateSelectVault
import proton.android.pass.featuremigrate.impl.MigrateVaultFilter
import proton.android.pass.featuremigrate.impl.migrateGraph
import proton.android.pass.featureonboarding.impl.OnBoarding
import proton.android.pass.featureonboarding.impl.onBoardingGraph
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheet
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.featurepassword.impl.dialog.mode.PasswordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.WordSeparatorDialog
import proton.android.pass.featurepassword.impl.generatePasswordBottomsheetGraph
import proton.android.pass.featureprofile.impl.AppLockTimeBottomsheet
import proton.android.pass.featureprofile.impl.AppLockTypeBottomsheet
import proton.android.pass.featureprofile.impl.ENTER_PIN_PARAMETER_KEY
import proton.android.pass.featureprofile.impl.FeedbackBottomsheet
import proton.android.pass.featureprofile.impl.PinConfig
import proton.android.pass.featureprofile.impl.Profile
import proton.android.pass.featureprofile.impl.ProfileNavigation
import proton.android.pass.featureprofile.impl.profileGraph
import proton.android.pass.featuresearchoptions.impl.FilterBottomsheet
import proton.android.pass.featuresearchoptions.impl.SearchOptionsBottomsheet
import proton.android.pass.featuresearchoptions.impl.SearchOptionsNavigation
import proton.android.pass.featuresearchoptions.impl.SortingBottomsheet
import proton.android.pass.featuresearchoptions.impl.SortingLocation
import proton.android.pass.featuresearchoptions.impl.searchOptionsGraph
import proton.android.pass.featuresettings.impl.ClearClipboardOptions
import proton.android.pass.featuresettings.impl.ClipboardSettings
import proton.android.pass.featuresettings.impl.DefaultVault
import proton.android.pass.featuresettings.impl.LogView
import proton.android.pass.featuresettings.impl.Settings
import proton.android.pass.featuresettings.impl.SettingsNavigation
import proton.android.pass.featuresettings.impl.ThemeSelector
import proton.android.pass.featuresettings.impl.settingsGraph
import proton.android.pass.featuresharing.impl.AcceptInvite
import proton.android.pass.featuresharing.impl.InviteConfirmed
import proton.android.pass.featuresharing.impl.InvitesInfoDialog
import proton.android.pass.featuresharing.impl.ManageVault
import proton.android.pass.featuresharing.impl.REFRESH_MEMBER_LIST_FLAG
import proton.android.pass.featuresharing.impl.ShareFromItem
import proton.android.pass.featuresharing.impl.SharingNavigation
import proton.android.pass.featuresharing.impl.SharingPermissions
import proton.android.pass.featuresharing.impl.SharingSummary
import proton.android.pass.featuresharing.impl.SharingWith
import proton.android.pass.featuresharing.impl.manage.bottomsheet.ConfirmTransferOwnership
import proton.android.pass.featuresharing.impl.manage.bottomsheet.InviteOptionsBottomSheet
import proton.android.pass.featuresharing.impl.manage.bottomsheet.InviteTypeValue
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberOptionsBottomSheet
import proton.android.pass.featuresharing.impl.sharingGraph
import proton.android.pass.featuresync.impl.SyncDialog
import proton.android.pass.featuresync.impl.SyncNavigation
import proton.android.pass.featuresync.impl.syncGraph
import proton.android.pass.featuretrial.impl.TrialNavigation
import proton.android.pass.featuretrial.impl.TrialScreen
import proton.android.pass.featuretrial.impl.trialGraph
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.featurevault.impl.bottomsheet.CreateVaultNextAction
import proton.android.pass.featurevault.impl.bottomsheet.CreateVaultScreen
import proton.android.pass.featurevault.impl.bottomsheet.EditVaultScreen
import proton.android.pass.featurevault.impl.bottomsheet.options.VaultOptionsBottomSheet
import proton.android.pass.featurevault.impl.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.featurevault.impl.delete.DeleteVaultDialog
import proton.android.pass.featurevault.impl.leave.LeaveVaultDialog
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.AppNavigation

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongMethod", "ComplexMethod")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
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

                        ItemTypeUiState.CreditCard -> throw NotImplementedError()
                    }

                    appNavigator.navigate(destination, route)
                }

                HomeNavigation.CreateVault -> {
                    appNavigator.navigate(
                        destination = CreateVaultScreen,
                        route = CreateVaultScreen.buildRoute(CreateVaultNextAction.Done)
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

                is HomeNavigation.EditCreditCard -> appNavigator.navigate(
                    EditCreditCard,
                    EditCreditCard.createNavRoute(it.shareId, it.itemId)
                )

                is HomeNavigation.ItemDetail -> {
                    appNavigator.navigate(
                        ViewItem,
                        ViewItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                HomeNavigation.Profile -> {
                    appNavigator.navigate(Profile)
                }

                is HomeNavigation.SortingBottomsheet -> {
                    appNavigator.navigate(
                        SortingBottomsheet,
                        SortingBottomsheet.createNavRoute(
                            location = SortingLocation.Home
                        )
                    )
                }

                is HomeNavigation.VaultOptions -> appNavigator.navigate(
                    VaultOptionsBottomSheet,
                    VaultOptionsBottomSheet.createNavRoute(it.shareId)
                )

                HomeNavigation.TrialInfo -> appNavigator.navigate(TrialScreen)
                HomeNavigation.OpenInvite -> appNavigator.navigate(
                    destination = AcceptInvite,
                    backDestination = Home
                )

                HomeNavigation.Finish -> onNavigate(AppNavigation.Finish)
                HomeNavigation.SyncDialog -> appNavigator.navigate(SyncDialog)
                HomeNavigation.OnBoarding -> appNavigator.navigate(OnBoarding)
                HomeNavigation.ConfirmedInvite -> appNavigator.navigate(
                    destination = InviteConfirmed
                )

                is HomeNavigation.SearchOptions -> appNavigator.navigate(
                    destination = SearchOptionsBottomsheet,
                    route = SearchOptionsBottomsheet.createRoute(it.bulkActionsEnabled),
                    backDestination = Home
                )

                HomeNavigation.MoveToVault -> appNavigator.navigate(
                    destination = MigrateSelectVault,
                    route = MigrateSelectVault.createNavRouteForMigrateSelectedItems(
                        filter = MigrateVaultFilter.All
                    )
                )
            }
        }
    )
    searchOptionsGraph(
        onNavigateEvent = {
            when (it) {
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                SearchOptionsNavigation.Filter -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = FilterBottomsheet,
                        backDestination = Home
                    )
                }

                SearchOptionsNavigation.Sorting -> dismissBottomSheet {
                    appNavigator.navigate(
                        SortingBottomsheet,
                        SortingBottomsheet.createNavRoute(
                            location = SortingLocation.Home
                        ),
                        Home
                    )
                }

                SearchOptionsNavigation.Dismiss -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                SearchOptionsNavigation.BulkActions -> dismissBottomSheet {
                    appNavigator.navigateBackWithResult(
                        key = HOME_ENABLE_BULK_ACTIONS_KEY,
                        value = true,
                        comesFromBottomsheet = true
                    )
                }
            }
        }
    )
    bottomsheetCreateItemGraph(
        mode = CreateItemBottomSheetMode.Full,
        onNavigate = {
            dismissBottomSheet {
                when (it) {
                    is CreateItemBottomsheetNavigation.CreateAlias ->
                        appNavigator.navigate(
                            CreateAlias,
                            CreateAlias.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateLogin ->
                        appNavigator.navigate(
                            CreateLogin,
                            CreateLogin.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateNote ->
                        appNavigator.navigate(
                            CreateNote,
                            CreateNote.createNavRoute(it.shareId)
                        )

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

                    is CreateItemBottomsheetNavigation.CreateCreditCard ->
                        appNavigator.navigate(
                            CreateCreditCard,
                            CreateCreditCard.createNavRoute(it.shareId)
                        )
                }
            }
        },
    )
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> dismissBottomSheet {
                    appNavigator.navigateBack()
                }

                VaultNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.navigateBackWithResult(
                        key = KEY_VAULT_SELECTED,
                        value = it.shareId.id,
                        comesFromBottomsheet = true
                    )
                }

                is VaultNavigation.VaultEdit -> dismissBottomSheet {
                    appNavigator.navigate(
                        EditVaultScreen,
                        EditVaultScreen.createNavRoute(it.shareId)
                    )
                }

                is VaultNavigation.VaultMigrate -> appNavigator.navigate(
                    MigrateSelectVault,
                    MigrateSelectVault.createNavRouteForMigrateAll(it.shareId)
                )

                is VaultNavigation.VaultRemove -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = DeleteVaultDialog,
                        route = DeleteVaultDialog.createNavRoute(it.shareId),
                        backDestination = Home
                    )
                }

                is VaultNavigation.VaultShare -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SharingWith,
                        route = SharingWith.createRoute(
                            shareId = it.shareId,
                            showEditVault = it.showEditVault
                        )
                    )
                }

                is VaultNavigation.VaultLeave -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = LeaveVaultDialog,
                        route = LeaveVaultDialog.createNavRoute(it.shareId),
                        backDestination = Home
                    )
                }

                is VaultNavigation.VaultAccess -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = ManageVault,
                        route = ManageVault.createRoute(it.shareId),
                        backDestination = Home
                    )
                }
            }
        }
    )
    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.navigateBack()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
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
                AccountNavigation.Back -> appNavigator.navigateBack()
                AccountNavigation.ConfirmSignOut -> onNavigate(AppNavigation.SignOut())
                AccountNavigation.DismissDialog -> appNavigator.navigateBack()
                AccountNavigation.SignOut -> appNavigator.navigate(SignOutDialog)
                AccountNavigation.Subscription -> onNavigate(AppNavigation.Subscription)
                AccountNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            }
        }
    )
    profileGraph(
        onNavigateEvent = {
            when (it) {
                ProfileNavigation.Account -> appNavigator.navigate(Account)
                ProfileNavigation.Settings -> appNavigator.navigate(Settings)
                ProfileNavigation.List -> appNavigator.navigate(Home)
                ProfileNavigation.CreateItem -> appNavigator.navigate(CreateItemBottomsheet)
                ProfileNavigation.Feedback -> appNavigator.navigate(FeedbackBottomsheet)
                ProfileNavigation.Report -> dismissBottomSheet {
                    onNavigate(AppNavigation.Report)
                }

                ProfileNavigation.FeatureFlags -> appNavigator.navigate(FeatureFlagRoute)
                ProfileNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                ProfileNavigation.Finish -> onNavigate(AppNavigation.Finish)
                ProfileNavigation.CloseBottomSheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                ProfileNavigation.AppLockTime -> appNavigator.navigate(AppLockTimeBottomsheet)
                ProfileNavigation.AppLockType -> appNavigator.navigate(AppLockTypeBottomsheet)
                ProfileNavigation.Back -> appNavigator.navigateBack()
                ProfileNavigation.ConfigurePin -> dismissBottomSheet {
                    appNavigator.navigate(PinConfig)
                }

                ProfileNavigation.EnterPin -> dismissBottomSheet {
                    appNavigator.navigate(EnterPin)
                }
            }
        }
    )
    settingsGraph(
        onNavigate = {
            when (it) {
                SettingsNavigation.SelectTheme -> appNavigator.navigate(ThemeSelector)
                SettingsNavigation.Close -> appNavigator.navigateBack()
                SettingsNavigation.DismissBottomSheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                SettingsNavigation.ViewLogs -> appNavigator.navigate(LogView)
                SettingsNavigation.ClipboardSettings -> dismissBottomSheet {
                    appNavigator.navigate(ClipboardSettings)
                }

                SettingsNavigation.ClearClipboardSettings -> dismissBottomSheet {
                    appNavigator.navigate(ClearClipboardOptions)
                }

                SettingsNavigation.Restart -> onNavigate(AppNavigation.Restart)
                SettingsNavigation.DefaultVault -> appNavigator.navigate(DefaultVault)
                SettingsNavigation.SyncDialog -> appNavigator.navigate(SyncDialog)
            }
        }
    )
    createUpdateLoginGraph(
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateLogin) -> CreateLogin
                appNavigator.hasDestinationInStack(EditLogin) -> EditLogin
                else -> null
            }
            when (it) {
                BaseLoginNavigation.Close -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(
                        it.shareId,
                        it.showUpgrade,
                        it.title
                    ),
                    backDestination = backDestination
                )

                BaseLoginNavigation.GeneratePassword ->
                    appNavigator.navigate(
                        destination = GeneratePasswordBottomsheet,
                        route = GeneratePasswordBottomsheet.buildRoute(
                            mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                        )
                    )

                is BaseLoginNavigation.OnCreateLoginEvent -> when (val event = it.event) {
                    is CreateLoginNavigation.LoginCreated -> appNavigator.navigateBack()
                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                        )
                    }
                }

                is BaseLoginNavigation.OnUpdateLoginEvent -> when (val event = it.event) {
                    is UpdateLoginNavigation.LoginUpdated -> {
                        appNavigator.navigate(
                            destination = ViewItem,
                            route = ViewItem.createNavRoute(event.shareId, event.itemId),
                            backDestination = Home
                        )
                    }
                }

                is BaseLoginNavigation.ScanTotp -> appNavigator.navigate(
                    destination = CameraTotp,
                    route = CameraTotp.createNavRoute(it.index)
                )

                BaseLoginNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)

                is BaseLoginNavigation.AliasOptions -> appNavigator.navigate(
                    destination = AliasOptionsBottomSheet,
                    route = AliasOptionsBottomSheet.createNavRoute(it.shareId, it.showUpgrade)
                )

                BaseLoginNavigation.DeleteAlias ->
                    appNavigator.navigateBackWithResult(CLEAR_ALIAS_NAV_PARAMETER_KEY, true)

                is BaseLoginNavigation.EditAlias -> {
                    appNavigator.navigate(
                        destination = CreateAliasBottomSheet,
                        route = CreateAliasBottomSheet.createNavRoute(
                            it.shareId,
                            it.showUpgrade,
                            isEdit = true
                        ),
                        backDestination = backDestination
                    )
                }

                BaseLoginNavigation.AddCustomField -> appNavigator.navigate(
                    destination = AddCustomFieldBottomSheet
                )

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialog,
                        route = CustomFieldNameDialog.buildRoute(it.type),
                        backDestination = backDestination
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheet,
                    route = CustomFieldOptionsBottomSheet.buildRoute(it.index, it.currentValue)
                )

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialog,
                        route = EditCustomFieldNameDialog.buildRoute(it.index, it.currentValue),
                        backDestination = backDestination
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is BaseLoginNavigation.OpenImagePicker -> {
                    appNavigator.navigate(
                        destination = PhotoPickerTotp,
                        route = PhotoPickerTotp.createNavRoute(it.index),
                        backDestination = backDestination
                    )
                }

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess -> appNavigator.navigateBackWithResult(it.results)
            }
        }
    )
    createNoteGraph(
        onNavigate = {
            when (it) {
                CreateNoteNavigation.Back -> appNavigator.navigateBack()
                is CreateNoteNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )

                CreateNoteNavigation.NoteCreated -> appNavigator.navigateBack()
            }
        }
    )
    updateNoteGraph(
        onNavigate = {
            when (it) {
                UpdateNoteNavigation.Back -> appNavigator.navigateBack()
                is UpdateNoteNavigation.NoteUpdated -> appNavigator.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(it.shareId, it.itemId),
                    backDestination = Home
                )
            }
        }
    )
    createCreditCardGraph {
        when (it) {
            BaseCreditCardNavigation.Close -> appNavigator.navigateBack()
            is CreateCreditCardNavigation -> when (it) {
                is CreateCreditCardNavigation.ItemCreated -> appNavigator.navigateBack()
                is CreateCreditCardNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )
            }

            BaseCreditCardNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            is UpdateCreditCardNavigation -> {}
        }
    }
    updateCreditCardGraph {
        when (it) {
            BaseCreditCardNavigation.Close -> appNavigator.navigateBack()
            is CreateCreditCardNavigation -> {}
            is UpdateCreditCardNavigation -> {
                when (it) {
                    is UpdateCreditCardNavigation.ItemUpdated -> appNavigator.navigate(
                        destination = ViewItem,
                        route = ViewItem.createNavRoute(it.shareId, it.itemId),
                        backDestination = Home
                    )
                }
            }

            BaseCreditCardNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }
    createAliasGraph(
        onNavigate = {
            when (it) {
                CreateAliasNavigation.Close -> appNavigator.navigateBack()
                CreateAliasNavigation.CloseBottomsheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is CreateAliasNavigation.CreatedFromBottomsheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is CreateAliasNavigation.Created -> {
                    appNavigator.navigateBack()
                }

                CreateAliasNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)

                is CreateAliasNavigation.SelectVault -> {
                    appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                    )
                }
            }
        },
    )
    updateAliasGraph(
        onNavigate = {
            when (it) {
                UpdateAliasNavigation.Close -> appNavigator.navigateBack()
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
                    appNavigator.navigateBack()
                }

                is ItemDetailNavigation.OnCreateLoginFromAlias -> {
                    appNavigator.navigate(
                        destination = CreateLogin,
                        route = CreateLogin.createNavRoute(
                            username = it.alias.some(),
                            shareId = it.shareId.toOption()
                        ),
                        backDestination = Home
                    )
                }

                is ItemDetailNavigation.OnEdit -> {
                    val destination = when (it.itemUiModel.contents) {
                        is ItemContents.Login -> EditLogin
                        is ItemContents.Note -> EditNote
                        is ItemContents.Alias -> EditAlias
                        is ItemContents.CreditCard -> EditCreditCard
                        is ItemContents.Unknown -> null
                    }
                    val route = when (it.itemUiModel.contents) {
                        is ItemContents.Login -> EditLogin.createNavRoute(
                            it.itemUiModel.shareId,
                            it.itemUiModel.id
                        )

                        is ItemContents.Note -> EditNote.createNavRoute(
                            it.itemUiModel.shareId,
                            it.itemUiModel.id
                        )

                        is ItemContents.Alias -> EditAlias.createNavRoute(
                            it.itemUiModel.shareId,
                            it.itemUiModel.id
                        )

                        is ItemContents.CreditCard -> EditCreditCard.createNavRoute(
                            it.itemUiModel.shareId,
                            it.itemUiModel.id
                        )

                        is ItemContents.Unknown -> null
                    }

                    if (destination != null && route != null) {
                        appNavigator.navigate(destination, route)
                    }
                }

                is ItemDetailNavigation.OnMigrate -> {
                    appNavigator.navigate(
                        destination = MigrateSelectVault,
                        route = MigrateSelectVault.createNavRouteForMigrateSelectedItems(
                            filter = MigrateVaultFilter.All
                        )
                    )
                }

                is ItemDetailNavigation.OnViewItem -> {
                    appNavigator.navigate(
                        destination = ViewItem,
                        route = ViewItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is ItemDetailNavigation.Upgrade -> {
                    if (it.popBefore) {
                        appNavigator.navigateBack()
                    }
                    onNavigate(AppNavigation.Upgrade)
                }

                is ItemDetailNavigation.ManageVault -> {
                    appNavigator.navigate(
                        destination = ManageVault,
                        route = ManageVault.createRoute(it.shareId)
                    )
                }

                is ItemDetailNavigation.OnShareVault -> {
                    appNavigator.navigate(
                        destination = ShareFromItem,
                        route = ShareFromItem.buildRoute(
                            shareId = it.shareId,
                            itemId = it.itemId
                        )
                    )
                }

                is ItemDetailNavigation.CannotPerformAction -> {
                    appNavigator.navigate(
                        destination = ItemDetailCannotPerformAction,
                        route = ItemDetailCannotPerformAction.buildRoute(it.type)
                    )
                }
            }
        }
    )

    migrateGraph(
        navigation = {
            when (it) {
                is MigrateNavigation.VaultSelectedForMigrateItem -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateConfirmVault,
                        route = MigrateConfirmVault.createNavRouteForMigrateSelectedItems(
                            destShareId = it.destShareId
                        ),
                        backDestination = ViewItem
                    )
                }

                is MigrateNavigation.ItemMigrated -> dismissBottomSheet {
                    // Only navigate to detail if we already were in a detail screen
                    if (appNavigator.hasDestinationInStack(ViewItem)) {
                        appNavigator.navigate(
                            destination = ViewItem,
                            route = ViewItem.createNavRoute(it.shareId, it.itemId),
                            backDestination = Home
                        )
                    } else if (appNavigator.hasDestinationInStack(Home)) {
                        appNavigator.popUpTo(Home, comesFromBottomsheet = true)
                    }
                }


                MigrateNavigation.VaultMigrated -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is MigrateNavigation.VaultSelectedForMigrateAll -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateConfirmVault,
                        route = MigrateConfirmVault.createNavRouteForMigrateAll(
                            sourceShareId = it.sourceShareId,
                            destShareId = it.destShareId
                        ),
                        backDestination = Home
                    )
                }

                MigrateNavigation.Close -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }
            }
        }
    )
    authGraph(
        canLogout = true,
        appNavigator = appNavigator,
        dismissBottomSheet = dismissBottomSheet,
        navigation = {
            when (it) {
                AuthNavigation.Back -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Success -> {
                    when {
                        appNavigator.hasPreviousDestination(AppLockTypeBottomsheet) ||
                            appNavigator.hasPreviousDestination(Profile) ->
                            appNavigator.navigateBackWithResult(
                                key = ENTER_PIN_PARAMETER_KEY,
                                value = true
                            )

                        else -> appNavigator.navigateBack()
                    }
                }

                AuthNavigation.Dismissed -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Failed -> appNavigator.navigateBack()
                AuthNavigation.SignOut -> appNavigator.navigate(SignOutDialog)
                AuthNavigation.ForceSignOut -> onNavigate(AppNavigation.SignOut())
                AuthNavigation.EnterPin -> appNavigator.navigate(EnterPin)
            }
        }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.navigate(Home) },
        onNavigateBack = { onNavigate(AppNavigation.Finish) }
    )
    featureFlagsGraph()
    trialGraph {
        when (it) {
            TrialNavigation.Close -> appNavigator.navigateBack()
            TrialNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }
    sharingGraph {
        when (it) {
            SharingNavigation.Back -> dismissBottomSheet {
                appNavigator.popUpTo(Home)
            }

            SharingNavigation.Upgrade -> dismissBottomSheet {
                onNavigate(AppNavigation.Upgrade)
            }

            is SharingNavigation.ShowInvitesInfo -> appNavigator.navigate(
                destination = InvitesInfoDialog,
                route = InvitesInfoDialog.buildRoute(it.shareId),
                backDestination = ManageVault
            )

            is SharingNavigation.CloseBottomSheet -> dismissBottomSheet {
                if (it.refresh) {
                    appNavigator.navigateBackWithResult(
                        key = REFRESH_MEMBER_LIST_FLAG,
                        value = true,
                        comesFromBottomsheet = true
                    )
                } else {
                    appNavigator.navigateBack(true)
                }
            }

            is SharingNavigation.Permissions -> appNavigator.navigate(
                destination = SharingPermissions,
                route = SharingPermissions.createRoute(
                    shareId = it.shareId,
                    email = it.email,
                    mode = it.mode
                )
            )

            is SharingNavigation.Summary -> appNavigator.navigate(
                destination = SharingSummary,
                route = SharingSummary.createRoute(
                    shareId = it.shareId,
                    email = it.email,
                    permission = it.permission,
                    mode = it.mode
                )
            )

            is SharingNavigation.ShareVault -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = SharingWith,
                    route = SharingWith.createRoute(
                        shareId = it.shareId,
                        showEditVault = false
                    )
                )
            }

            is SharingNavigation.ManageVault -> appNavigator.navigate(
                destination = ManageVault,
                route = ManageVault.createRoute(it.shareId),
                backDestination = Home
            )

            is SharingNavigation.MemberOptions -> appNavigator.navigate(
                destination = MemberOptionsBottomSheet,
                route = MemberOptionsBottomSheet.buildRoute(
                    shareId = it.shareId,
                    memberShareId = it.destShareId,
                    shareRole = it.memberRole,
                    memberEmail = it.destEmail
                )
            )

            is SharingNavigation.ExistingUserInviteOptions -> appNavigator.navigate(
                destination = InviteOptionsBottomSheet,
                route = InviteOptionsBottomSheet.buildRoute(
                    shareId = it.shareId,
                    inviteType = InviteTypeValue.ExistingUserInvite(it.inviteId)
                )
            )

            is SharingNavigation.NewUserInviteOptions -> appNavigator.navigate(
                destination = InviteOptionsBottomSheet,
                route = InviteOptionsBottomSheet.buildRoute(
                    shareId = it.shareId,
                    inviteType = InviteTypeValue.NewUserInvite(it.inviteId)
                )
            )

            is SharingNavigation.TransferOwnershipConfirm -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = ConfirmTransferOwnership,
                    route = ConfirmTransferOwnership.buildRoute(
                        shareId = it.shareId,
                        memberShareId = it.destShareId,
                        memberEmail = it.destEmail
                    ),
                    backDestination = ManageVault
                )
            }

            is SharingNavigation.MoveItemToSharedVault -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = MigrateSelectVault,
                    route = MigrateSelectVault.createNavRouteForMigrateSelectedItems(
                        filter = MigrateVaultFilter.Shared
                    )
                )
            }

            is SharingNavigation.CreateVaultAndMoveItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = CreateVaultScreen,
                    route = CreateVaultScreen.buildRoute(
                        nextAction = CreateVaultNextAction.ShareVault(
                            shareId = it.shareId,
                            itemId = it.itemId
                        )
                    )
                )
            }

            is SharingNavigation.EditVault -> appNavigator.navigate(
                destination = EditVaultScreen,
                route = EditVaultScreen.createNavRoute(it.shareId),
                backDestination = SharingWith
            )

            is SharingNavigation.ViewVault -> dismissBottomSheet {
                when {
                    appNavigator.hasDestinationInStack(Home) -> {
                        appNavigator.navigateBackWithResult(
                            key = HOME_GO_TO_VAULT_KEY,
                            value = it.shareId.id,
                            comesFromBottomsheet = true
                        )
                    }

                    else -> {
                        appNavigator.navigate(
                            destination = Home,
                            route = Home.buildRoute(it.shareId)
                        )
                    }
                }

            }
        }
    }
    syncGraph {
        when (it) {
            SyncNavigation.FinishedFetching -> appNavigator.navigateBack()
        }
    }
}
