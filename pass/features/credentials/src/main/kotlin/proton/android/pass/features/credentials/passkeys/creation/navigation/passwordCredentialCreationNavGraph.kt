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
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.credentials.passwords.creation.navigation.PasswordCredentialCreationNavEvent
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.CreateLoginNavigation
import proton.android.pass.features.itemcreate.login.EditLoginNavItem
import proton.android.pass.features.itemcreate.login.InitialCreateLoginUiState
import proton.android.pass.features.itemcreate.login.createUpdateLoginGraph
import proton.android.pass.features.itemcreate.totp.CameraTotpNavItem
import proton.android.pass.features.itemcreate.totp.PhotoPickerTotpNavItem
import proton.android.pass.features.password.GeneratePasswordBottomsheet
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.features.password.GeneratePasswordNavigation
import proton.android.pass.features.password.dialog.mode.PasswordModeDialog
import proton.android.pass.features.password.dialog.separator.WordSeparatorDialog
import proton.android.pass.features.password.generatePasswordBottomsheetGraph
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongMethod")
internal fun NavGraphBuilder.passwordCredentialCreationNavGraph(
    appNavigator: AppNavigator,
    initialCreateLoginUiState: InitialCreateLoginUiState,
    onNavigate: (PasswordCredentialCreationNavEvent) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = { destination ->
            when (destination) {
                is AuthNavigation.CloseScreen,
                AuthNavigation.Dismissed,
                AuthNavigation.Failed -> onNavigate(PasswordCredentialCreationNavEvent.Cancel)

                is AuthNavigation.Success -> dismissBottomSheet {
                    appNavigator.navigate(SelectItem)
                }

                is AuthNavigation.ForceSignOut -> {
                    PasswordCredentialCreationNavEvent.ForceSignOut(
                        userId = destination.userId
                    ).also(onNavigate)
                }

                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(
                        origin = destination.origin
                    )
                )

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> Unit

                AuthNavigation.CloseBottomsheet -> dismissBottomSheet {}
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
                        onNavigate(PasswordCredentialCreationNavEvent.SendResponse)
                    }

                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        throw IllegalStateException("Cannot create Passkey on PasswordCredentialCreation")
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
                    onNavigate(PasswordCredentialCreationNavEvent.Upgrade)
                }

                is BaseLoginNavigation.AliasOptions,
                BaseLoginNavigation.DeleteAlias,
                is BaseLoginNavigation.EditAlias -> Unit

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
                is BaseLoginNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(
                            sectionIndex = None,
                            index = destination.index
                        ),
                        backDestination = CreateLoginNavItem
                    )
                }

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess -> appNavigator.navigateBackWithResult(
                    values = destination.results
                )

                BaseLoginNavigation.AddAttachment,
                BaseLoginNavigation.UpsellAttachments,
                is BaseLoginNavigation.OpenAttachmentOptions,
                is BaseLoginNavigation.OpenDraftAttachmentOptions,
                is BaseLoginNavigation.DeleteAllAttachments -> {
                    throw IllegalStateException("Cannot use attachments from PasswordCredentialCreation")
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

    vaultGraph(
        onNavigate = { destination ->
            when (destination) {
                VaultNavigation.CloseScreen -> appNavigator.navigateBack()
                VaultNavigation.DismissBottomsheet -> dismissBottomSheet {}
                VaultNavigation.Upgrade -> onNavigate(PasswordCredentialCreationNavEvent.Upgrade)
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
