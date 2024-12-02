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

package proton.android.pass.autofill.ui.autofill

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.Utils
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.toSelectItemState
import proton.android.pass.autofill.extensions.CreatedAlias
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.extensions.toAutofillItem
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.some
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsBottomSheetNavItem
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.ItemOptionsNavDestination
import proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.navigation.itemOptionsNavGraph
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureitemcreate.impl.alias.CreateAlias
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasNavigation
import proton.android.pass.featureitemcreate.impl.alias.createAliasGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomSheetMode.AutofillCreditCard
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomSheetMode.AutofillIdentity
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomSheetMode.AutofillLogin
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavItem
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.bottomsheetCreateItemGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCard
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.UpdateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.createCreditCardGraph
import proton.android.pass.featureitemcreate.impl.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentity
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.UpdateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.bottomsheets.IdentityFieldsBottomSheet
import proton.android.pass.featureitemcreate.impl.identity.navigation.createIdentityGraph
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.CustomSectionNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.CustomSectionOptionsBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.EditCustomSectionNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.CreateLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.login.createUpdateLoginGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
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

sealed interface AutofillEvent {
    data object SelectItemScreenShown : AutofillEvent

    @JvmInline
    value class AutofillItemSelected(val item: AutofillItem) : AutofillEvent

    @JvmInline
    value class AutofillSuggestionSelected(val item: AutofillItem) : AutofillEvent
}

@Suppress("LongParameterList", "LongMethod", "ComplexMethod", "ThrowsCount")
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.autofillActivityGraph(
    appNavigator: AppNavigator,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    onNavigate: (AutofillNavigation) -> Unit,
    onEvent: (AutofillEvent) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    val mode = when (autofillAppState.autofillData.assistInfo.cluster) {
        is NodeCluster.CreditCard -> AutofillCreditCard
        is NodeCluster.Login,
        is NodeCluster.SignUp -> AutofillLogin

        is NodeCluster.Identity -> AutofillIdentity

        NodeCluster.Empty -> AutofillLogin
    }
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                is AuthNavigation.Back -> onNavigate(AutofillNavigation.Cancel)
                is AuthNavigation.Success -> when {
                    selectedAutofillItem != null ->
                        onEvent(AutofillEvent.AutofillSuggestionSelected(selectedAutofillItem))

                    else -> appNavigator.navigate(SelectItem)
                }

                AuthNavigation.Dismissed -> onNavigate(AutofillNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(AutofillNavigation.Cancel)
                is AuthNavigation.ForceSignOut -> onNavigate(AutofillNavigation.ForceSignOut(it.userId))
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )

                is AuthNavigation.SignOut,
                AuthNavigation.ForceSignOutAllUsers -> {
                }

                AuthNavigation.CloseBottomsheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }
            }
        }
    )
    selectItemGraph(
        state = autofillAppState.toSelectItemState(),
        onScreenShown = { onEvent(AutofillEvent.SelectItemScreenShown) },
        onNavigate = {
            when (it) {
                SelectItemNavigation.AddItem -> appNavigator.navigate(
                    CreateItemBottomsheetNavItem,
                    CreateItemBottomsheetNavItem.createNavRoute(mode)
                )
                SelectItemNavigation.Cancel -> onNavigate(AutofillNavigation.Cancel)
                is SelectItemNavigation.ItemSelected -> {
                    onEvent(AutofillEvent.AutofillItemSelected(it.item.toAutoFillItem()))
                }

                is SelectItemNavigation.SuggestionSelected -> {
                    onEvent(AutofillEvent.AutofillSuggestionSelected(it.item.toAutoFillItem()))
                }

                is SelectItemNavigation.SortingBottomsheet ->
                    appNavigator.navigate(
                        SortingBottomsheetNavItem,
                        SortingBottomsheetNavItem.createNavRoute(
                            location = SortingLocation.Autofill
                        )
                    )

                is SelectItemNavigation.ItemOptions -> appNavigator.navigate(
                    destination = ItemOptionsBottomSheetNavItem,
                    route = ItemOptionsBottomSheetNavItem.createRoute(it.userId, it.shareId, it.itemId)
                )

                SelectItemNavigation.Upgrade -> onNavigate(AutofillNavigation.Upgrade)
                SelectItemNavigation.SelectAccount -> appNavigator.navigate(AccountSwitchNavItem)
            }
        }
    )
    searchOptionsGraph(
        onNavigateEvent = {
            when (it) {
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                SearchOptionsNavigation.ResetFilters,
                SearchOptionsNavigation.Filter,
                SearchOptionsNavigation.Sorting,
                SearchOptionsNavigation.BulkActions -> throw IllegalStateException("Action not supported")
            }
        }
    )

    createUpdateLoginGraph(
        initialCreateLoginUiState = InitialCreateLoginUiState(
            title = run {
                val url = autofillAppState.autofillData.assistInfo.url
                val appName = autofillAppState.autofillData.packageInfo.appName.value
                Utils.getTitle(url, appName.some())
            },
            url = autofillAppState.autofillData.assistInfo.url.value(),
            aliasItemFormState = null,

            // Only pass PackageInfoUi if the packageName is not a browser
            packageInfoUi = autofillAppState.autofillData.packageInfo
                .takeIf { !it.packageName.isBrowser() }
                ?.let { PackageInfoUi(it) }
        ),
        onNavigate = {
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
                        onEvent(AutofillEvent.AutofillItemSelected(event.itemUiModel.toAutoFillItem()))
                    }

                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        throw IllegalStateException("Cannot create login with passkey from autofill")
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

                BaseLoginNavigation.Upgrade -> onNavigate(AutofillNavigation.Upgrade)

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

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

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

                BaseLoginNavigation.AddAttachment -> {
                    throw IllegalStateException("Cannot add attachment from autofill")
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
                    val created = CreatedAlias(it.shareId, it.itemId, it.alias)
                    onEvent(AutofillEvent.AutofillItemSelected(created.toAutofillItem()))
                }

                CreateAliasNavigation.Upgrade -> onNavigate(AutofillNavigation.Upgrade)
                is CreateAliasNavigation.SelectVault -> {
                    appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                    )
                }

                CreateAliasNavigation.AddAttachment -> {
                    throw IllegalStateException("Cannot add attachment from autofill")
                }
            }
        }
    )
    createCreditCardGraph {
        when (it) {
            BaseCreditCardNavigation.Close -> appNavigator.navigateBack()
            is CreateCreditCardNavigation -> when (it) {
                is CreateCreditCardNavigation.ItemCreated ->
                    onEvent(AutofillEvent.AutofillItemSelected(it.itemUiModel.toAutoFillItem()))

                is CreateCreditCardNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )
            }

            BaseCreditCardNavigation.Upgrade -> onNavigate(AutofillNavigation.Upgrade)
            is UpdateCreditCardNavigation -> {}
            BaseCreditCardNavigation.AddAttachment -> {
                throw IllegalStateException("Cannot add attachment from autofill")
            }
        }
    }
    createIdentityGraph(
        onNavigate = {
            when (it) {
                BaseIdentityNavigation.Close -> appNavigator.navigateBack()
                is BaseIdentityNavigation.OpenExtraFieldBottomSheet ->
                    appNavigator.navigate(
                        destination = IdentityFieldsBottomSheet,
                        route = IdentityFieldsBottomSheet.createRoute(
                            it.addIdentityFieldType,
                            it.sectionIndex
                        )
                    )

                is CreateIdentityNavigation.ItemCreated ->
                    onEvent(AutofillEvent.AutofillItemSelected(it.itemUiModel.toAutoFillItem()))

                is CreateIdentityNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )

                BaseIdentityNavigation.OpenCustomFieldBottomSheet ->
                    dismissBottomSheet { appNavigator.navigate(AddCustomFieldBottomSheetNavItem.CreateIdentity) }

                is BaseIdentityNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem.CreateIdentity,
                        route = CustomFieldNameDialogNavItem.CreateIdentity.buildRoute(it.type),
                        backDestination = CreateIdentity
                    )
                }

                is BaseIdentityNavigation.EditCustomField -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem.CreateIdentity,
                        route = EditCustomFieldNameDialogNavItem.CreateIdentity.buildRoute(
                            it.index,
                            it.title
                        ),
                        backDestination = CreateIdentity
                    )
                }

                is BaseIdentityNavigation.CustomFieldOptions -> appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem.CreateIdentity,
                    route = CustomFieldOptionsBottomSheetNavItem.CreateIdentity.buildRoute(
                        it.index,
                        it.title
                    )
                )

                BaseIdentityNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                BaseIdentityNavigation.AddExtraSection ->
                    appNavigator.navigate(CustomSectionNameDialogNavItem)

                is BaseIdentityNavigation.EditCustomSection -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomSectionNameDialogNavItem,
                        route = EditCustomSectionNameDialogNavItem.buildRoute(it.index, it.title),
                        backDestination = CreateIdentity
                    )
                }

                is BaseIdentityNavigation.ExtraSectionOptions -> appNavigator.navigate(
                    destination = CustomSectionOptionsBottomSheetNavItem,
                    route = CustomSectionOptionsBottomSheetNavItem.buildRoute(it.index, it.title)
                )

                BaseIdentityNavigation.RemoveCustomSection -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is UpdateIdentityNavigation.IdentityUpdated -> {
                    // Updates cannot happen
                }

                BaseIdentityNavigation.AddAttachment -> {
                    throw IllegalStateException("Cannot add attachment from autofill")
                }
            }
        }
    )
    bottomsheetCreateItemGraph(
        onNavigate = {
            when (it) {
                is CreateItemBottomsheetNavigation.CreateAlias -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CreateAlias,
                        route = CreateAlias.createNavRoute(it.shareId)
                    )
                }

                is CreateItemBottomsheetNavigation.CreateLogin -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CreateLogin,
                        route = CreateLogin.createNavRoute(it.shareId)
                    )
                }

                is CreateItemBottomsheetNavigation.CreateCreditCard -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CreateCreditCard,
                        route = CreateCreditCard.createNavRoute(it.shareId)
                    )
                }

                is CreateItemBottomsheetNavigation.CreateNote ->
                    throw IllegalStateException("Cannot create note from autofill bottomsheet")

                CreateItemBottomsheetNavigation.CreatePassword ->
                    throw IllegalStateException("Cannot create password from autofill bottomsheet")

                is CreateItemBottomsheetNavigation.CreateIdentity -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = CreateIdentity,
                        route = CreateIdentity.createNavRoute(it.shareId)
                    )
                }
            }
        }
    )

    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.navigateBack()
                VaultNavigation.Upgrade -> onNavigate(AutofillNavigation.Upgrade)
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
                    CreateItemBottomsheetNavItem,
                    CreateItemBottomsheetNavItem.createNavRoute(mode)
                )
            }
        }
    }
}
