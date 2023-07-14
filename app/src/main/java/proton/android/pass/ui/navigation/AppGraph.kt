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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.featuresearchoptions.impl.SortingBottomsheet
import proton.android.featuresearchoptions.impl.SortingLocation
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
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurefeatureflags.impl.FeatureFlagRoute
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
import proton.android.pass.featureitemcreate.impl.note.createNoteGraph
import proton.android.pass.featureitemcreate.impl.note.updateNoteGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.INDEX_NAV_PARAMETER_KEY
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
import proton.android.pass.featureprofile.impl.AppLockConfig
import proton.android.pass.featureprofile.impl.AppLockTimeBottomsheet
import proton.android.pass.featureprofile.impl.AppLockTypeBottomsheet
import proton.android.pass.featureprofile.impl.FeedbackBottomsheet
import proton.android.pass.featureprofile.impl.PinConfig
import proton.android.pass.featureprofile.impl.Profile
import proton.android.pass.featureprofile.impl.ProfileNavigation
import proton.android.pass.featureprofile.impl.profileGraph
import proton.android.pass.featuresettings.impl.ClearClipboardOptions
import proton.android.pass.featuresettings.impl.ClipboardSettings
import proton.android.pass.featuresettings.impl.LogView
import proton.android.pass.featuresettings.impl.SelectPrimaryVault
import proton.android.pass.featuresettings.impl.Settings
import proton.android.pass.featuresettings.impl.SettingsNavigation
import proton.android.pass.featuresettings.impl.ThemeSelector
import proton.android.pass.featuresettings.impl.settingsGraph
import proton.android.pass.featuretrial.impl.TrialNavigation
import proton.android.pass.featuretrial.impl.TrialScreen
import proton.android.pass.featuretrial.impl.trialGraph
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.featurevault.impl.bottomsheet.CreateVaultBottomSheet
import proton.android.pass.featurevault.impl.bottomsheet.EditVaultBottomSheet
import proton.android.pass.featurevault.impl.bottomsheet.options.VaultOptionsBottomSheet
import proton.android.pass.featurevault.impl.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.featurevault.impl.delete.DeleteVaultDialog
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.AppNavigation
import proton.android.pass.ui.RootNavigation
import proton.android.pass.ui.rootGraph
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Suppress("LongParameterList", "LongMethod", "ComplexMethod")
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    rootGraph(
        onNavigateEvent = {
            when (it) {
                RootNavigation.Auth -> appNavigator.navigate(Auth)
                RootNavigation.Home -> appNavigator.navigate(Home)
                RootNavigation.OnBoarding -> appNavigator.navigate(OnBoarding)
            }
        }
    )
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
                    appNavigator.navigate(CreateVaultBottomSheet)
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
                            sortingType = it.searchSortingType,
                            location = SortingLocation.Home
                        )
                    )
                }

                is HomeNavigation.VaultOptions -> appNavigator.navigate(
                    VaultOptionsBottomSheet,
                    VaultOptionsBottomSheet.createNavRoute(it.shareId)
                )

                HomeNavigation.TrialInfo -> appNavigator.navigate(TrialScreen)
                HomeNavigation.Finish -> onNavigate(AppNavigation.Finish)
            }
        }
    )
    sortingGraph(
        onNavigateEvent = {
            when (it) {
                is SortingNavigation.SelectSorting -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }
            }
        }
    )
    bottomsheetCreateItemGraph(
        mode = CreateItemBottomSheetMode.Full,
        onNavigate = {
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
        },
    )
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.onBackClick()
                VaultNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.navigateUpWithResult(
                        key = KEY_VAULT_SELECTED,
                        value = it.shareId.id,
                        comesFromBottomsheet = true
                    )
                }

                is VaultNavigation.VaultEdit -> appNavigator.navigate(
                    EditVaultBottomSheet,
                    EditVaultBottomSheet.createNavRoute(it.shareId)
                )

                is VaultNavigation.VaultMigrate -> appNavigator.navigate(
                    MigrateSelectVault,
                    MigrateSelectVault.createNavRouteForMigrateAll(it.shareId)
                )

                is VaultNavigation.VaultRemove -> appNavigator.navigate(
                    destination = DeleteVaultDialog,
                    route = DeleteVaultDialog.createNavRoute(it.shareId),
                    backDestination = Home
                )
            }
        }
    )
    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.onBackClick()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
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
        onNavigateEvent = {
            when (it) {
                ProfileNavigation.Account -> appNavigator.navigate(Account)
                ProfileNavigation.Settings -> appNavigator.navigate(Settings)
                ProfileNavigation.List -> appNavigator.navigate(Home)
                ProfileNavigation.CreateItem -> appNavigator.navigate(CreateItemBottomsheet)
                ProfileNavigation.Feedback -> appNavigator.navigate(FeedbackBottomsheet)
                ProfileNavigation.AppLockConfig -> appNavigator.navigate(AppLockConfig)
                ProfileNavigation.Report -> dismissBottomSheet {
                    onNavigate(AppNavigation.Report)
                }

                ProfileNavigation.FeatureFlags -> appNavigator.navigate(FeatureFlagRoute)
                ProfileNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                ProfileNavigation.Finish -> onNavigate(AppNavigation.Finish)
                ProfileNavigation.CloseBottomSheet -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }

                ProfileNavigation.AppLockTime -> appNavigator.navigate(AppLockTimeBottomsheet)
                ProfileNavigation.AppLockType -> appNavigator.navigate(AppLockTypeBottomsheet)
                ProfileNavigation.Back -> appNavigator.onBackClick()
                ProfileNavigation.PinConfig -> appNavigator.navigate(PinConfig)
            }
        }
    )
    settingsGraph(
        onNavigate = {
            when (it) {
                SettingsNavigation.SelectTheme -> appNavigator.navigate(ThemeSelector)
                SettingsNavigation.Close -> appNavigator.onBackClick()
                SettingsNavigation.DismissBottomSheet -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }

                SettingsNavigation.ViewLogs -> appNavigator.navigate(LogView)
                SettingsNavigation.ClipboardSettings -> appNavigator.navigate(ClipboardSettings)
                SettingsNavigation.ClearClipboardSettings -> dismissBottomSheet {
                    appNavigator.navigate(ClearClipboardOptions)
                }

                SettingsNavigation.PrimaryVault -> appNavigator.navigate(SelectPrimaryVault)
                SettingsNavigation.Restart -> onNavigate(AppNavigation.Restart)
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
                    appNavigator.onBackClick(comesFromBottomsheet = true)
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
                    is CreateLoginNavigation.LoginCreated -> appNavigator.onBackClick()
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
                    appNavigator.navigateUpWithResult(CLEAR_ALIAS_NAV_PARAMETER_KEY, true)

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
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }
            }
        }
    )
    createTotpGraph(
        onSuccess = { totp, index ->
            val values = mutableMapOf<String, Any>(TOTP_NAV_PARAMETER_KEY to totp)
            index?.let { values.put(INDEX_NAV_PARAMETER_KEY, it) }
            appNavigator.navigateUpWithResult(values)
        },
        onCloseTotp = { appNavigator.onBackClick() },
        onOpenImagePicker = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateLogin) -> CreateLogin
                appNavigator.hasDestinationInStack(EditLogin) -> EditLogin
                else -> null
            }
            appNavigator.navigate(
                destination = PhotoPickerTotp,
                route = PhotoPickerTotp.createNavRoute(it.toOption()),
                backDestination = backDestination
            )
        }
    )
    createNoteGraph(
        onNavigate = {
            when (it) {
                CreateNoteNavigation.Back -> appNavigator.onBackClick()
                is CreateNoteNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )

                CreateNoteNavigation.Success -> appNavigator.onBackClick()
            }
        }
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
    createCreditCardGraph {
        when (it) {
            BaseCreditCardNavigation.Close -> appNavigator.onBackClick()
            is CreateCreditCardNavigation -> when (it) {
                is CreateCreditCardNavigation.ItemCreated -> appNavigator.onBackClick()
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
            BaseCreditCardNavigation.Close -> appNavigator.onBackClick()
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
                CreateAliasNavigation.Close -> appNavigator.onBackClick()
                CreateAliasNavigation.CloseBottomsheet -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }

                is CreateAliasNavigation.CreatedFromBottomsheet -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }

                is CreateAliasNavigation.Created -> {
                    appNavigator.onBackClick()
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

                ItemDetailNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
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

                MigrateNavigation.VaultMigrated -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
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

                MigrateNavigation.Close -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }
            }
        }
    )

    authGraph(
        canLogout = true,
        navigation = {
            when (it) {
                AuthNavigation.Back -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Success -> appNavigator.onBackClick()
                AuthNavigation.Dismissed -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Failed -> appNavigator.onBackClick()
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
            TrialNavigation.Close -> appNavigator.onBackClick()
            TrialNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }
}
