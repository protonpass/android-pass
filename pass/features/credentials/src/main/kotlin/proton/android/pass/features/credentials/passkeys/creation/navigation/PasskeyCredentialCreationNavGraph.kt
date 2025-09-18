/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passkeys.creation.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationEvent
import proton.android.pass.features.itemcreate.alias.AliasSelectMailboxBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.AliasSelectSuffixBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.alias.CreateAliasNavItem
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
import proton.android.pass.features.itemcreate.login.InitialCreateLoginUiState
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.createUpdateLoginGraph
import proton.android.pass.features.itemcreate.totp.CameraTotpNavItem
import proton.android.pass.features.itemcreate.totp.PhotoPickerTotpNavItem
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
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.navigation.selectItemGraph
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongMethod", "LongParameterList", "ThrowsCount")
internal fun NavGraphBuilder.passkeyCredentialCreationNavGraph(
    appNavigator: AppNavigator,
    initialCreateLoginUiState: InitialCreateLoginUiState,
    selectItemState: SelectItemState,
    onNavigate: (PasskeyCredentialCreationNavEvent) -> Unit,
    onEvent: (PasskeyCredentialCreationEvent) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    accountSwitchNavGraph { destination ->
        when (destination) {
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

    createAliasGraph(
        canUseAttachments = false,
        canAddMailbox = false,
        onNavigate = { destination ->
            when (destination) {
                is BaseAliasNavigation.OnCreateAliasEvent -> when (val event = destination.event) {
                    is CreateAliasNavigation.Created ->
                        throw IllegalStateException("Cannot create alias from PasskeyCredentialCreation")
                    is CreateAliasNavigation.CreatedFromBottomsheet -> dismissBottomSheet {}
                    is CreateAliasNavigation.SelectVault -> appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                    )
                }
                is BaseAliasNavigation.OnUpdateAliasEvent ->
                    throw IllegalStateException("Cannot update alias from PasskeyCredentialCreation")
                BaseAliasNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseAliasNavigation.CloseBottomsheet -> dismissBottomSheet {}
                BaseAliasNavigation.Upgrade -> onNavigate(PasskeyCredentialCreationNavEvent.Upgrade)
                BaseAliasNavigation.SelectMailbox -> appNavigator.navigate(
                    destination = AliasSelectMailboxBottomSheetNavItem
                )
                BaseAliasNavigation.SelectSuffix -> appNavigator.navigate(
                    destination = AliasSelectSuffixBottomSheetNavItem
                )
                BaseAliasNavigation.AddAttachment,
                BaseAliasNavigation.UpsellAttachments,
                is BaseAliasNavigation.OpenDraftAttachmentOptions,
                is BaseAliasNavigation.DeleteAllAttachments ->
                    throw IllegalStateException("Cannot use attachments from PasskeyCredentialCreation")
                BaseAliasNavigation.AddMailbox ->
                    throw IllegalStateException("Cannot add mailbox from PasskeyCredentialCreation")

                BaseAliasNavigation.AddCustomField -> appNavigator.navigate(
                    destination = AddCustomFieldBottomSheetNavItem.CreateAlias,
                    route = AddCustomFieldBottomSheetNavItem.CreateAlias.buildRoute(None)
                )

                is BaseAliasNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem.CreateAlias,
                        route = CustomFieldNameDialogNavItem.CreateAlias.buildRoute(
                            type = destination.type,
                            sectionIndex = None
                        ),
                        backDestination = CreateAliasNavItem
                    )
                }

                is BaseAliasNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem.CreateAlias,
                    route = CustomFieldOptionsBottomSheetNavItem.CreateAlias.buildRoute(
                        index = destination.index,
                        sectionIndex = None,
                        currentTitle = destination.currentValue
                    )
                )

                is BaseAliasNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem.CreateAlias,
                        route = EditCustomFieldNameDialogNavItem.CreateAlias.buildRoute(
                            index = destination.index,
                            sectionIndex = None,
                            currentValue = destination.currentValue
                        ),
                        backDestination = CreateAliasNavItem
                    )
                }

                BaseAliasNavigation.RemovedCustomField -> dismissBottomSheet {}

                BaseAliasNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseAliasNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(destination.results)

                is BaseAliasNavigation.OpenImagePicker -> appNavigator.navigate(
                    destination = PhotoPickerTotpNavItem.CreateAlias,
                    route = PhotoPickerTotpNavItem.CreateAlias.createNavRoute(
                        sectionIndex = None,
                        index = destination.index
                    ),
                    backDestination = CreateAliasNavItem
                )

                is BaseAliasNavigation.ScanTotp -> appNavigator.navigate(
                    destination = CameraTotpNavItem(CustomFieldPrefix.CreateAlias),
                    route = CameraTotpNavItem(CustomFieldPrefix.CreateAlias)
                        .createNavRoute(index = destination.index)
                )
            }
        }
    )

    createUpdateLoginGraph(
        initialCreateLoginUiState = initialCreateLoginUiState,
        showCreateAliasButton = true,
        canUseAttachments = false,
        onNavigate = { destination ->
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateLoginNavItem) -> CreateLoginNavItem
                appNavigator.hasDestinationInStack(EditLoginNavItem) -> EditLoginNavItem
                else -> null
            }
            when (destination) {
                BaseLoginNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseLoginNavigation.DismissBottomsheet -> dismissBottomSheet {}

                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(
                        shareId = destination.shareId,
                        showUpgrade = destination.showUpgrade,
                        title = destination.title
                    )
                )

                BaseLoginNavigation.GeneratePassword -> appNavigator.navigate(
                    destination = GeneratePasswordBottomsheet,
                    route = GeneratePasswordBottomsheet.buildRoute(
                        mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                    )
                )

                is BaseLoginNavigation.OnCreateLoginEvent -> when (val event = destination.event) {
                    is CreateLoginNavigation.LoginCreated -> {
                        throw IllegalStateException("Should not invoke this on PasskeyCredentialCreation")
                    }

                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        PasskeyCredentialCreationNavEvent.SendResponse(
                            response = event.createPasskeyResponse
                        ).also(onNavigate)
                    }

                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(
                                selectedVault = event.shareId
                            )
                        )
                    }
                }

                is BaseLoginNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(
                            sectionIndex = None,
                            index = destination.index
                        )
                    )
                }

                BaseLoginNavigation.Upgrade -> {
                    onNavigate(PasskeyCredentialCreationNavEvent.Upgrade)
                }

                is BaseLoginNavigation.AliasOptions -> appNavigator.navigate(
                    destination = AliasOptionsBottomSheet,
                    route = AliasOptionsBottomSheet.createNavRoute(
                        shareId = destination.shareId,
                        showUpgrade = destination.showUpgrade
                    )
                )

                BaseLoginNavigation.DeleteAlias -> appNavigator.navigateBackWithResult(
                    key = CLEAR_ALIAS_NAV_PARAMETER_KEY,
                    value = true
                )

                is BaseLoginNavigation.EditAlias -> {
                    appNavigator.navigate(
                        destination = CreateAliasBottomSheet,
                        route = CreateAliasBottomSheet.createNavRoute(
                            shareId = destination.shareId,
                            showUpgrade = destination.showUpgrade,
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
                            type = destination.type,
                            sectionIndex = None
                        ),
                        backDestination = CreateLoginNavItem
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem.CreateLogin,
                    route = CustomFieldOptionsBottomSheetNavItem.CreateLogin.buildRoute(
                        index = destination.index,
                        sectionIndex = None,
                        currentTitle = destination.currentValue
                    )
                )

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem.CreateLogin,
                        route = EditCustomFieldNameDialogNavItem.CreateLogin.buildRoute(
                            index = destination.index,
                            sectionIndex = None,
                            currentValue = destination.currentValue
                        ),
                        backDestination = CreateLoginNavItem
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {}

                // Updates cannot happen
                is BaseLoginNavigation.OnUpdateLoginEvent -> Unit
                is BaseLoginNavigation.OpenImagePicker -> appNavigator.navigate(
                    destination = PhotoPickerTotpNavItem.CreateLogin,
                    route = PhotoPickerTotpNavItem.CreateLogin.createNavRoute(
                        sectionIndex = None,
                        index = destination.index
                    ),
                    backDestination = CreateLoginNavItem
                )

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess -> appNavigator.navigateBackWithResult(
                    values = destination.results
                )

                BaseLoginNavigation.AddAttachment,
                BaseLoginNavigation.UpsellAttachments,
                is BaseLoginNavigation.OpenAttachmentOptions,
                is BaseLoginNavigation.OpenDraftAttachmentOptions,
                is BaseLoginNavigation.DeleteAllAttachments -> {
                    throw IllegalStateException("Cannot use attachments from PasskeyCredentialCreation")
                }
            }
        }
    )

    generatePasswordBottomsheetGraph(
        onNavigate = { destination ->
            when (destination) {
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

    itemOptionsNavGraph { destination ->
        when (destination) {
            ItemOptionsNavDestination.Dismiss -> dismissBottomSheet {}
        }
    }

    searchOptionsGraph(
        onNavigateEvent = { destination ->
            when (destination) {
                SearchOptionsNavigation.ResetFilters,
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {}

                SearchOptionsNavigation.Filter -> {
                    throw IllegalStateException("Cannot Filter on PasskeyCredentialCreation")
                }

                SearchOptionsNavigation.Sorting -> {
                    throw IllegalStateException("Cannot change Sorting on PasskeyCredentialCreation")
                }

                SearchOptionsNavigation.BulkActions -> {
                    throw IllegalStateException("Cannot perform bulk actions on PasskeyCredentialCreation")
                }
            }
        }
    )

    selectItemGraph(
        state = selectItemState,
        onScreenShown = {},
        onNavigate = { destination ->
            when (destination) {
                SelectItemNavigation.AddItem -> {
                    appNavigator.navigate(
                        destination = CreateLoginNavItem,
                        route = CreateLoginNavItem.createNavRoute()
                    )
                }

                SelectItemNavigation.Cancel -> {
                    onNavigate(PasskeyCredentialCreationNavEvent.Cancel)
                }

                is SelectItemNavigation.ItemSelected -> {
                    PasskeyCredentialCreationEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                is SelectItemNavigation.SuggestionSelected -> {
                    PasskeyCredentialCreationEvent.OnItemSelected(
                        itemUiModel = destination.item
                    ).also(onEvent)
                }

                is SelectItemNavigation.SortingBottomsheet -> appNavigator.navigate(
                    destination = SortingBottomsheetNavItem,
                    route = SortingBottomsheetNavItem.createNavRoute(
                        location = SortingLocation.Autofill
                    )
                )

                is SelectItemNavigation.ItemOptions -> appNavigator.navigate(
                    destination = ItemOptionsBottomSheetNavItem,
                    route = ItemOptionsBottomSheetNavItem.createRoute(
                        destination.userId,
                        destination.shareId,
                        destination.itemId
                    )
                )

                SelectItemNavigation.Upgrade -> {
                    onNavigate(PasskeyCredentialCreationNavEvent.Upgrade)
                }

                SelectItemNavigation.SelectAccount -> appNavigator.navigate(AccountSwitchNavItem)
            }
        }
    )

    vaultGraph(
        onNavigate = { destination ->
            when (destination) {
                VaultNavigation.CloseScreen -> appNavigator.navigateBack()
                VaultNavigation.DismissBottomsheet -> dismissBottomSheet {}
                VaultNavigation.Upgrade -> onNavigate(PasskeyCredentialCreationNavEvent.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.setResult(
                        values = mapOf(KEY_VAULT_SELECTED to destination.shareId.id)
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
}
