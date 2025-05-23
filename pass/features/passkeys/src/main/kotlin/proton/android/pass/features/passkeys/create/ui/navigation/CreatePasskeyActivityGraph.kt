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
import proton.android.pass.common.api.None
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.itemcreate.alias.AliasSelectMailboxBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.AliasSelectSuffixBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.alias.CreateAliasNavigation
import proton.android.pass.features.itemcreate.alias.createAliasGraph
import proton.android.pass.features.itemcreate.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.cannotcreateitems.navigation.CannotCreateItemsNavDestination
import proton.android.pass.features.itemcreate.dialogs.cannotcreateitems.navigation.CannotCreateItemsNavItem
import proton.android.pass.features.itemcreate.dialogs.cannotcreateitems.navigation.cannotCreateItemsNavGraph
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.CreateLoginNavigation
import proton.android.pass.features.itemcreate.login.EditLoginNavItem
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.createUpdateLoginGraph
import proton.android.pass.features.itemcreate.totp.CameraTotpNavItem
import proton.android.pass.features.itemcreate.totp.PhotoPickerTotpNavItem
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
                is AuthNavigation.CloseScreen -> onNavigate(CreatePasskeyNavigation.Cancel)
                is AuthNavigation.Success -> dismissBottomSheet {
                    appNavigator.navigate(SelectItem)
                }
                AuthNavigation.Dismissed -> onNavigate(CreatePasskeyNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(CreatePasskeyNavigation.Cancel)

                is AuthNavigation.ForceSignOut ->
                    onNavigate(CreatePasskeyNavigation.ForceSignOut(it.userId))

                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> {
                }

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
                        destination = CreateLoginNavItem,
                        route = CreateLoginNavItem.createNavRoute()
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
                    route = ItemOptionsBottomSheetNavItem.createRoute(
                        it.userId,
                        it.shareId,
                        it.itemId
                    )
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
        showCreateAliasButton = true,
        canUseAttachments = false,
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateLoginNavItem) -> CreateLoginNavItem
                appNavigator.hasDestinationInStack(EditLoginNavItem) -> EditLoginNavItem
                else -> null
            }
            when (it) {
                BaseLoginNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseLoginNavigation.DismissBottomsheet -> dismissBottomSheet {}

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

                is BaseLoginNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(None, it.index)
                    )
                }

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
                    destination = AddCustomFieldBottomSheetNavItem.CreateLogin,
                    route = AddCustomFieldBottomSheetNavItem.CreateLogin.buildRoute(None)
                )

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem.CreateLogin,
                        route = CustomFieldNameDialogNavItem.CreateLogin.buildRoute(
                            type = it.type,
                            sectionIndex = None
                        ),
                        backDestination = CreateLoginNavItem
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem.CreateLogin,
                    route = CustomFieldOptionsBottomSheetNavItem.CreateLogin.buildRoute(
                        index = it.index,
                        sectionIndex = None,
                        currentTitle = it.currentValue
                    )
                )

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem.CreateLogin,
                        route = EditCustomFieldNameDialogNavItem.CreateLogin.buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentValue = it.currentValue
                        ),
                        backDestination = CreateLoginNavItem
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {}

                // Updates cannot happen
                is BaseLoginNavigation.OnUpdateLoginEvent -> {}
                is BaseLoginNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(None, it.index),
                        backDestination = CreateLoginNavItem
                    )
                }

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
        canUseAttachments = false,
        canAddMailbox = false,
        onNavigate = {
            when (it) {
                CreateAliasNavigation.CloseScreen -> appNavigator.navigateBack()
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

                CreateAliasNavigation.SelectMailbox ->
                    appNavigator.navigate(AliasSelectMailboxBottomSheetNavItem)

                CreateAliasNavigation.SelectSuffix ->
                    appNavigator.navigate(AliasSelectSuffixBottomSheetNavItem)

                CreateAliasNavigation.AddAttachment,
                CreateAliasNavigation.UpsellAttachments,
                is CreateAliasNavigation.OpenDraftAttachmentOptions,
                is CreateAliasNavigation.DeleteAllAttachments -> {
                    throw IllegalStateException("Cannot use attachments from CreatePasskey")
                }

                CreateAliasNavigation.AddMailbox ->
                    throw IllegalStateException("Cannot add mailbox from CreatePasskey")
            }
        }
    )

    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.CloseScreen -> appNavigator.navigateBack()
                VaultNavigation.DismissBottomsheet -> dismissBottomSheet {}
                VaultNavigation.Upgrade -> onNavigate(CreatePasskeyNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.setResult(
                        mapOf(KEY_VAULT_SELECTED to it.shareId.id)
                    )
                }

                is VaultNavigation.VaultEdit,
                is VaultNavigation.VaultMigrate,
                is VaultNavigation.VaultMigrateSharedWarning,
                is VaultNavigation.VaultRemove,
                is VaultNavigation.VaultShare,
                is VaultNavigation.VaultLeave,
                is VaultNavigation.VaultAccess -> Unit
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
            ItemOptionsNavDestination.Dismiss -> dismissBottomSheet {}
        }
    }

    accountSwitchNavGraph {
        when (it) {
            AccountSwitchNavigation.CreateItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = CreateLoginNavItem,
                    route = CreateLoginNavItem.createNavRoute()
                )
            }

            AccountSwitchNavigation.CannotCreateItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = CannotCreateItemsNavItem
                )
            }
        }
    }

    cannotCreateItemsNavGraph { destination ->
        when (destination) {
            CannotCreateItemsNavDestination.Back -> appNavigator.navigateBack()
        }
    }
}
