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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import proton.android.pass.autofill.entities.usernamePassword
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureauth.impl.AUTH_SCREEN_ROUTE
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.AddCustomFieldBottomSheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.dialogs.CustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.dialogs.EditCustomFieldNameDialog
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.CreateLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.createUpdateLoginGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheet
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.featurepassword.impl.dialog.mode.PasswordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.WordSeparatorDialog
import proton.android.pass.featurepassword.impl.generatePasswordBottomsheetGraph
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.featurevault.impl.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("ComplexMethod", "LongMethod")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.autosaveActivityGraph(
    appNavigator: AppNavigator,
    arguments: AutoSaveArguments,
    onNavigate: (AutosaveNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    composable(AUTH_SCREEN_ROUTE) {
        AuthScreen(
            canLogout = false,
            navigation = {
                when (it) {
                    AuthNavigation.Back -> onNavigate(AutosaveNavigation.Cancel)
                    AuthNavigation.Success -> appNavigator.navigate(CreateLogin)
                    AuthNavigation.Dismissed -> onNavigate(AutosaveNavigation.Cancel)
                    AuthNavigation.Failed -> onNavigate(AutosaveNavigation.Cancel)
                    AuthNavigation.SignOut -> {}
                    AuthNavigation.ForceSignOut -> onNavigate(AutosaveNavigation.ForceSignOut)
                }
            }
        )
    }
    createUpdateLoginGraph(
        initialCreateLoginUiState = getInitialState(arguments),
        showCreateAliasButton = false,
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> dismissBottomSheet {
                    onNavigate(AutosaveNavigation.Cancel)
                }

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

                BaseLoginNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)

                BaseLoginNavigation.AddCustomField -> appNavigator.navigate(
                    destination = AddCustomFieldBottomSheet
                )

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialog,
                        route = CustomFieldNameDialog.buildRoute(it.type),
                        backDestination = CreateLogin
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
                        backDestination = CreateLogin
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.onBackClick(comesFromBottomsheet = true)
                }

                // Updates cannot happen
                is BaseLoginNavigation.OnUpdateLoginEvent -> {}
                // Aliases not allowed
                is BaseLoginNavigation.AliasOptions -> {}
                BaseLoginNavigation.DeleteAlias -> {}
                is BaseLoginNavigation.EditAlias -> {}
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
    createTotpGraph(
        onSuccess = { totp, index ->
            val values = mutableMapOf<String, Any>(TOTP_NAV_PARAMETER_KEY to totp)
            index?.let { values.put(INDEX_NAV_PARAMETER_KEY, it) }
            appNavigator.navigateUpWithResult(values)
        },
        onCloseTotp = { appNavigator.onBackClick() },
        onOpenImagePicker = {
            appNavigator.navigate(
                destination = PhotoPickerTotp,
                route = PhotoPickerTotp.createNavRoute(it.toOption()),
                backDestination = CreateLogin
            )
        }
    )

    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.onBackClick()
                VaultNavigation.Upgrade -> onNavigate(AutosaveNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.navigateUpWithResult(
                        key = KEY_VAULT_SELECTED,
                        value = it.shareId.id,
                        comesFromBottomsheet = true
                    )
                }

                is VaultNavigation.VaultEdit -> {}
                is VaultNavigation.VaultMigrate -> {}
                is VaultNavigation.VaultRemove -> {}
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
