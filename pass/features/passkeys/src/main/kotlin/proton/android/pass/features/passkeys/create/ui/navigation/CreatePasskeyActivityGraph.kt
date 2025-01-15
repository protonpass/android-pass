/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.passkeys.create.ui.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.alias.CreateAliasNavigation
import proton.android.pass.features.itemcreate.alias.createAliasGraph
import proton.android.pass.features.itemcreate.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation
import proton.android.pass.features.itemcreate.login.CreateLogin
import proton.android.pass.features.itemcreate.login.CreateLoginNavigation
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.createUpdateLoginGraph
import proton.android.pass.features.itemcreate.totp.CameraTotp
import proton.android.pass.features.itemcreate.totp.PhotoPickerTotp
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyNavState
import proton.android.pass.features.passkeys.create.ui.app.CreatePasskeyEvent
import proton.android.pass.features.passkeys.create.ui.app.CreatePasskeyNavigation
import proton.android.pass.features.password.GeneratePasswordBottomsheet
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.features.password.GeneratePasswordNavigation
import proton.android.pass.features.password.dialog.mode.PasswordModeDialog
import proton.android.pass.features.password.dialog.separator.WordSeparatorDialog
import proton.android.pass.features.password.generatePasswordBottomsheetGraph
import proton.android.pass.features.report.navigation.AccountSwitchNavItem
import proton.android.pass.features.report.navigation.AccountSwitchNavigation
import proton.android.pass.features.report.navigation.accountSwitchNavGraph
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongMethod", "CyclomaticComplexMethod", "ComplexMethod", "ThrowsCount")
fun NavGraphBuilder.createPasskeyActivityGraph(
    appNavigator: AppNavigator,
    navState: CreatePasskeyNavState.Ready,
    onEvent: (CreatePasskeyEvent) -> Unit,
    onNavigate: (CreatePasskeyNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                is AuthNavigation.Back -> onNavigate(CreatePasskeyNavigation.Cancel)
                is AuthNavigation.Success -> appNavigator.navigate(SelectItem)
                AuthNavigation.Dismissed -> onNavigate(CreatePasskeyNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(CreatePasskeyNavigation.Cancel)

                is AuthNavigation.ForceSignOut ->
                    onNavigate(CreatePasskeyNavigation.ForceSignOut(it.userId))
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> {}

                AuthNavigation.CloseBottomsheet -> dismissBottomSheet {}
            }
        }
    )

    selectItemGraph(
        state = navState.selectItemState,
        onScreenShown = {},
        onNavigate = {
            when (it) {
                SelectItemNavigation.AddItem -> {
                    appNavigator.navigate(
                        destination = CreateLogin,
                        route = CreateLogin.createNavRoute()
                    )
                }

                SelectItemNavigation.Cancel -> {
                    onNavigate(CreatePasskeyNavigation.Cancel)
                }

                is SelectItemNavigation.ItemSelected -> {
                    onEvent(CreatePasskeyEvent.OnItemSelected(it.item))
                }

                is SelectItemNavigation.SuggestionSelected -> {
                    onEvent(CreatePasskeyEvent.OnItemSelected(it.item))
                }

                is SelectItemNavigation.SortingBottomsheet -> appNavigator.navigate(
                    SortingBottomsheetNavItem,
                    SortingBottomsheetNavItem.createNavRoute(
                        location = SortingLocation.Autofill
                    )
                )

                is SelectItemNavigation.ItemOptions -> appNavigator.navigate(
                    destination = ItemOptionsBottomSheetNavItem,
                    route = ItemOptionsBottomSheetNavItem.createRoute(it.userId, it.shareId, it.itemId)
                )

                SelectItemNavigation.Upgrade -> {
                    onNavigate(CreatePasskeyNavigation.Upgrade)
                }

                SelectItemNavigation.SelectAccount -> appNavigator.navigate(AccountSwitchNavItem)
            }
        }
    )

    createUpdateLoginGraph(
        initialCreateLoginUiState = navState.createLoginUiState,
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> dismissBottomSheet {}

                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(
                        it.shareId,
                        it.showUpgrade,
                        it.title
                    )
                )

                BaseLoginNavigation.GeneratePassword -> appNavigator.navigate(
                    destination = GeneratePasswordBottomsheet,
                    route = GeneratePasswordBottomsheet.buildRoute(
                        mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                    )
                )

                is BaseLoginNavigation.OnCreateLoginEvent -> when (val event = it.event) {
                    is CreateLoginNavigation.LoginCreated -> {
                        throw IllegalStateException("Should not invoke this on CreatePasskey")
                    }

                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        onNavigate(CreatePasskeyNavigation.SendResponse(event.createPasskeyResponse))
                    }

                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                        )
                    }
                }

                is BaseLoginNavigation.ScanTotp -> appNavigator.navigate(
                    destination = CameraTotp,
                    route = CameraTotp.createNavRoute(it.index)
                )

                BaseLoginNavigation.Upgrade -> onNavigate(CreatePasskeyNavigation.Upgrade)

                is BaseLoginNavigation.AliasOptions -> appNavigator.navigate(
                    destination = AliasOptionsBottomSheet,
                    route = AliasOptionsBottomSheet.createNavRoute(it.shareId, it.showUpgrade)
                )

                BaseLoginNavigation.DeleteAlias -> appNavigator.navigateBackWithResult(
                    key = CLEAR_ALIAS_NAV_PARAMETER_KEY,
                    value = true
                )

                is BaseLoginNavigation.EditAlias -> {
                    appNavigator.navigate(
                        destination = CreateAliasBottomSheet,
                        route = CreateAliasBottomSheet.createNavRoute(
                            it.shareId,
                            it.showUpgrade,
                            isEdit = true
                        )
                    )
                }

                BaseLoginNavigation.AddCustomField -> appNavigator.navigate(
                    destination = AddCustomFieldBottomSheetNavItem.CreateLogin
                )

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem.CreateLogin,
                        route = CustomFieldNameDialogNavItem.CreateLogin.buildRoute(it.type),
                        backDestination = CreateLogin
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem.CreateLogin,
                    route = CustomFieldOptionsBottomSheetNavItem.CreateLogin.buildRoute(
                        it.index,
                        it.currentValue
                    )
                )

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem.CreateLogin,
                        route = EditCustomFieldNameDialogNavItem.CreateLogin.buildRoute(
                            it.index,
                            it.currentValue
                        ),
                        backDestination = CreateLogin
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {}

                // Updates cannot happen
                is BaseLoginNavigation.OnUpdateLoginEvent -> {}
                is BaseLoginNavigation.OpenImagePicker -> appNavigator.navigate(
                    destination = PhotoPickerTotp,
                    route = PhotoPickerTotp.createNavRoute(it.index),
                    backDestination = CreateLogin
                )

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)

                BaseLoginNavigation.AddAttachment,
                BaseLoginNavigation.UpsellAttachments,
                is BaseLoginNavigation.OpenAttachmentOptions,
                is BaseLoginNavigation.OpenDraftAttachmentOptions,
                is BaseLoginNavigation.DeleteAllAttachments -> {
                    throw IllegalStateException("Cannot use attachments from CreatePasskey")
                }
            }
        }
    )

    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.navigateBack()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {}

                GeneratePasswordNavigation.OnSelectWordSeparator -> appNavigator.navigate(
                    destination = WordSeparatorDialog
                )

                GeneratePasswordNavigation.OnSelectPasswordMode -> appNavigator.navigate(
                    destination = PasswordModeDialog
                )
            }
        }
    )

    createAliasGraph(
        onNavigate = {
            when (it) {
                CreateAliasNavigation.Close -> appNavigator.navigateBack()
                CreateAliasNavigation.CloseBottomsheet -> dismissBottomSheet {}

                is CreateAliasNavigation.CreatedFromBottomsheet -> dismissBottomSheet {}


                is CreateAliasNavigation.Created -> {
                    throw IllegalStateException("Cannot create alias from CreatePasskey")
                }

                CreateAliasNavigation.Upgrade -> onNavigate(CreatePasskeyNavigation.Upgrade)
                is CreateAliasNavigation.SelectVault -> {
                    appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                    )
                }

                CreateAliasNavigation.AddAttachment,
                CreateAliasNavigation.UpsellAttachments,
                is CreateAliasNavigation.OpenDraftAttachmentOptions,
                is CreateAliasNavigation.DeleteAllAttachments -> {
                    throw IllegalStateException("Cannot use attachments from CreatePasskey")
                }
            }
        }
    )

    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.navigateBack()
                VaultNavigation.Upgrade -> onNavigate(CreatePasskeyNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.navigateBackWithResult(
                        key = KEY_VAULT_SELECTED,
                        value = it.shareId.id,
                        comesFromBottomsheet = true
                    )
                }

                is VaultNavigation.VaultEdit,
                is VaultNavigation.VaultMigrate,
                is VaultNavigation.VaultRemove,
                is VaultNavigation.VaultShare,
                is VaultNavigation.VaultLeave,
                is VaultNavigation.VaultAccess -> {
                }
            }
        }
    )

    searchOptionsGraph(
        onNavigateEvent = {
            when (it) {
                SearchOptionsNavigation.ResetFilters,
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {}

                SearchOptionsNavigation.Filter -> {
                    throw IllegalStateException("Cannot Filter on CreatePasskey")
                }

                SearchOptionsNavigation.Sorting -> {
                    throw IllegalStateException("Cannot change Sorting on CreatePasskey")
                }

                SearchOptionsNavigation.BulkActions -> {
                    throw IllegalStateException("Cannot perform bulk actions on CreatePasskey")
                }
            }
        }
    )

    itemOptionsNavGraph { destination ->
        when (destination) {
            ItemOptionsNavDestination.Dismiss -> dismissBottomSheet {
                appNavigator.navigateBack(comesFromBottomsheet = true)
            }
        }
    }

    accountSwitchNavGraph {
        when (it) {
            AccountSwitchNavigation.CreateItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = CreateLogin,
                    route = CreateLogin.createNavRoute()
                )
            }
        }
    }
}
