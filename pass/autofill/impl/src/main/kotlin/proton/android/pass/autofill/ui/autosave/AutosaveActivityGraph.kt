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

package proton.android.pass.autofill.ui.autosave

import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.usernamePassword
import proton.android.pass.common.api.None
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.CreateLoginNavigation
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
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("ComplexMethod", "LongMethod")
fun NavGraphBuilder.autosaveActivityGraph(
    appNavigator: AppNavigator,
    arguments: AutoSaveArguments,
    onNavigate: (AutosaveNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                is AuthNavigation.CloseScreen -> onNavigate(AutosaveNavigation.Cancel)
                is AuthNavigation.Success -> dismissBottomSheet {
                    appNavigator.navigate(CreateLoginNavItem)
                }
                AuthNavigation.Dismissed -> onNavigate(AutosaveNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(AutosaveNavigation.Cancel)
                is AuthNavigation.ForceSignOut ->
                    onNavigate(AutosaveNavigation.ForceSignOut(it.userId))

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
    createUpdateLoginGraph(
        initialCreateLoginUiState = getInitialState(arguments),
        showCreateAliasButton = false,
        canUseAttachments = false,
        onNavigate = {
            when (it) {
                BaseLoginNavigation.CloseScreen -> onNavigate(AutosaveNavigation.Cancel)
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
                    is CreateLoginNavigation.LoginCreated -> onNavigate(AutosaveNavigation.Success)

                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        throw IllegalStateException("Cannot create login with passkey from autosave")
                    }

                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                        )
                    }
                }

                is BaseLoginNavigation.ScanTotp -> appNavigator.navigate(
                    destination = CameraTotpNavItem,
                    route = CameraTotpNavItem.createNavRoute(None, it.index)
                )

                BaseLoginNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)

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
                // Aliases not allowed
                is BaseLoginNavigation.AliasOptions -> {}
                BaseLoginNavigation.DeleteAlias -> {}
                is BaseLoginNavigation.EditAlias -> {}
                is BaseLoginNavigation.OpenImagePicker -> appNavigator.navigate(
                    destination = PhotoPickerTotpNavItem,
                    route = PhotoPickerTotpNavItem.createNavRoute(None, it.index),
                    backDestination = CreateLoginNavItem
                )

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)

                BaseLoginNavigation.AddAttachment,
                BaseLoginNavigation.UpsellAttachments,
                is BaseLoginNavigation.OpenAttachmentOptions,
                is BaseLoginNavigation.DeleteAllAttachments,
                is BaseLoginNavigation.OpenDraftAttachmentOptions ->
                    throw IllegalStateException("Cannot use attachments from autofill")
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
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.CloseScreen -> appNavigator.navigateBack()
                VaultNavigation.DismissBottomsheet -> dismissBottomSheet {}
                VaultNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)
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
}

private fun getInitialState(arguments: AutoSaveArguments): InitialCreateLoginUiState {
    val saveInformation = arguments.saveInformation
    val (username, password) = saveInformation.usernamePassword()
    val packageInfoUi = arguments.linkedAppInfo?.let {
        PackageInfoUi(packageName = it.packageName, appName = it.appName)
    }
    return InitialCreateLoginUiState(
        username = username,
        password = password,
        url = arguments.website,
        title = arguments.title,
        packageInfoUi = packageInfoUi
    )
}
