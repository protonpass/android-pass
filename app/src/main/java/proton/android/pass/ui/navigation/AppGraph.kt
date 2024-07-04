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

import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.featureaccount.impl.Account
import proton.android.pass.featureaccount.impl.AccountNavigation
import proton.android.pass.featureaccount.impl.accountGraph
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthOrigin
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurefeatureflags.impl.FeatureFlagRoute
import proton.android.pass.featurefeatureflags.impl.featureFlagsGraph
import proton.android.pass.featurehome.impl.HOME_ENABLE_BULK_ACTIONS_KEY
import proton.android.pass.featurehome.impl.HOME_GO_TO_VAULT_KEY
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.featurehome.impl.HomeNavigation
import proton.android.pass.featurehome.impl.HomeUpgradeDialog
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
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.common.CustomFieldPrefix
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCard
import proton.android.pass.featureitemcreate.impl.creditcard.CreateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.EditCreditCard
import proton.android.pass.featureitemcreate.impl.creditcard.UpdateCreditCardNavigation
import proton.android.pass.featureitemcreate.impl.creditcard.createCreditCardGraph
import proton.android.pass.featureitemcreate.impl.creditcard.updateCreditCardGraph
import proton.android.pass.featureitemcreate.impl.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentity
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.UpdateIdentity
import proton.android.pass.featureitemcreate.impl.identity.navigation.UpdateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.bottomsheets.IdentityFieldsBottomSheet
import proton.android.pass.featureitemcreate.impl.identity.navigation.createUpdateIdentityGraph
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.CustomSectionNameDialogNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.CustomSectionOptionsBottomSheetNavItem
import proton.android.pass.featureitemcreate.impl.identity.navigation.customsection.EditCustomSectionNameDialogNavItem
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
import proton.android.pass.featureitemdetail.impl.ItemDetailNavScope
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ViewItem
import proton.android.pass.featureitemdetail.impl.itemDetailGraph
import proton.android.pass.featureitemdetail.impl.login.passkey.bottomsheet.navigation.ViewPasskeyDetailsBottomSheet
import proton.android.pass.featureitemdetail.impl.login.reusedpass.navigation.LoginItemDetailsReusedPassNavItem
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
import proton.android.pass.features.extrapassword.ExtraPasswordNavigation
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordNavItem
import proton.android.pass.features.extrapassword.confirm.navigation.ConfirmExtraPasswordNavItem
import proton.android.pass.features.extrapassword.extraPasswordGraph
import proton.android.pass.features.extrapassword.infosheet.navigation.ExtraPasswordInfoNavItem
import proton.android.pass.features.extrapassword.options.navigation.ExtraPasswordOptionsNavItem
import proton.android.pass.features.item.details.detail.navigation.ItemDetailsNavItem
import proton.android.pass.features.item.details.detailmenu.navigation.ItemDetailsMenuNavItem
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination
import proton.android.pass.features.item.details.shared.navigation.itemDetailsNavGraph
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.navigation.itemHistoryNavGraph
import proton.android.pass.features.item.history.restore.navigation.ItemHistoryRestoreNavItem
import proton.android.pass.features.item.history.timeline.navigation.ItemHistoryTimelineNavItem
import proton.android.pass.features.secure.links.create.navigation.SecureLinksCreateNavItem
import proton.android.pass.features.secure.links.list.navigation.SecureLinksListNavItem
import proton.android.pass.features.secure.links.listmenu.navigation.SecureLinksListMenuNavItem
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewBottomSheetNavItem
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewNavScope
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewScreenNavItem
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksNavDestination
import proton.android.pass.features.secure.links.shared.navigation.secureLinksNavGraph
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterAliasAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterCustomAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterGlobalAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterProtonAddressOptionsNavItem
import proton.android.pass.features.security.center.aliaslist.navigation.SecurityCenterAliasListNavItem
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterAliasEmailBreachDetailNavItem
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterCustomEmailBreachDetailNavItem
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterProtonEmailBreachDetailNavItem
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavItem
import proton.android.pass.features.security.center.darkweb.navigation.CustomEmailOptionsNavItem
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebCannotAddCustomEmailNavItem
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavItem
import proton.android.pass.features.security.center.darkweb.navigation.help.DarkWebHelpNavItem
import proton.android.pass.features.security.center.excludeditems.navigation.SecurityCenterExcludedItemsNavItem
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavItem
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFANavItem
import proton.android.pass.features.security.center.protonlist.navigation.SecurityCenterProtonListNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterAliasEmailReportNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterCustomEmailReportNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterProtonEmailReportNavItem
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassNavItem
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelNavItem
import proton.android.pass.features.security.center.shared.navigation.SecurityCenterNavDestination
import proton.android.pass.features.security.center.shared.navigation.SecurityCenterNavDestination.ItemDetails.Origin
import proton.android.pass.features.security.center.shared.navigation.securityCenterNavGraph
import proton.android.pass.features.security.center.verifyemail.navigation.SecurityCenterVerifyEmailNavItem
import proton.android.pass.features.security.center.weakpass.navigation.SecurityCenterWeakPassNavItem
import proton.android.pass.features.upsell.navigation.UpsellNavDestination
import proton.android.pass.features.upsell.navigation.UpsellNavItem
import proton.android.pass.features.upsell.navigation.upsellNavGraph
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
import proton.android.pass.featuresharing.impl.InvitesErrorDialog
import proton.android.pass.featuresharing.impl.InvitesInfoDialog
import proton.android.pass.featuresharing.impl.ManageVault
import proton.android.pass.featuresharing.impl.REFRESH_MEMBER_LIST_FLAG
import proton.android.pass.featuresharing.impl.ShareFromItem
import proton.android.pass.featuresharing.impl.SharingNavigation
import proton.android.pass.featuresharing.impl.SharingPermissions
import proton.android.pass.featuresharing.impl.SharingSummary
import proton.android.pass.featuresharing.impl.SharingWith
import proton.android.pass.featuresharing.impl.extensions.toSharingType
import proton.android.pass.featuresharing.impl.manage.bottomsheet.ConfirmTransferOwnership
import proton.android.pass.featuresharing.impl.manage.bottomsheet.InviteOptionsBottomSheet
import proton.android.pass.featuresharing.impl.manage.bottomsheet.InviteTypeValue
import proton.android.pass.featuresharing.impl.manage.bottomsheet.MemberOptionsBottomSheet
import proton.android.pass.featuresharing.impl.sharingGraph
import proton.android.pass.featuresharing.impl.sharingpermissions.bottomsheet.SharingEditPermissions
import proton.android.pass.featuresync.impl.navigation.SyncNavDestination
import proton.android.pass.featuresync.impl.navigation.SyncNavItem
import proton.android.pass.featuresync.impl.navigation.syncNavGraph
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

@Suppress("LongMethod", "ComplexMethod", "ThrowsCount")
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

                        ItemTypeUiState.CreditCard ->
                            CreateCreditCard to CreateCreditCard.createNavRoute(it.shareId)

                        ItemTypeUiState.Identity ->
                            CreateIdentity to CreateIdentity.createNavRoute(it.shareId)
                    }

                    appNavigator.navigate(destination, route)
                }

                HomeNavigation.Back -> appNavigator.navigateBack(comesFromBottomsheet = false)

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

                is HomeNavigation.EditIdentity -> appNavigator.navigate(
                    UpdateIdentity,
                    UpdateIdentity.createNavRoute(it.shareId, it.itemId)
                )

                is HomeNavigation.ItemDetail -> appNavigator.navigate(
                    destination = getItemDetailsDestination(it.itemCategory),
                    route = getItemDetailsRoute(
                        itemCategory = it.itemCategory,
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )

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
                HomeNavigation.SyncDialog -> appNavigator.navigate(SyncNavItem)

                HomeNavigation.OnBoarding -> appNavigator.navigate(destination = OnBoarding)
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

                is HomeNavigation.ItemHistory -> appNavigator.navigate(
                    destination = ItemHistoryTimelineNavItem,
                    route = ItemHistoryTimelineNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )

                HomeNavigation.SecurityCenter -> appNavigator.navigate(
                    destination = SecurityCenterHomeNavItem
                )

                HomeNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)

                HomeNavigation.UpgradeDialog -> appNavigator.navigate(destination = HomeUpgradeDialog)
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

                    is CreateItemBottomsheetNavigation.CreateIdentity ->
                        appNavigator.navigate(
                            CreateIdentity,
                            CreateIdentity.createNavRoute(it.shareId)
                        )
                }
            }
        }
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
                AccountNavigation.SignOut -> onNavigate(AppNavigation.SignOut())
                AccountNavigation.Subscription -> onNavigate(AppNavigation.Subscription)
                AccountNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                AccountNavigation.PasswordManagement -> onNavigate(AppNavigation.PasswordManagement)
                AccountNavigation.RecoveryEmail -> onNavigate(AppNavigation.RecoveryEmail)
                AccountNavigation.SetExtraPassword -> appNavigator.navigate(ExtraPasswordInfoNavItem)
                is AccountNavigation.ExtraPasswordOptions ->
                    appNavigator.navigate(ExtraPasswordOptionsNavItem)
            }
        },
        subGraph = {
            extraPasswordGraph(
                onNavigate = {
                    when (it) {
                        ExtraPasswordNavigation.Back -> dismissBottomSheet { appNavigator.navigateBack() }
                        ExtraPasswordNavigation.Configure ->
                            dismissBottomSheet {
                                appNavigator.navigate(
                                    destination = Auth,
                                    route = Auth.buildRoute(AuthOrigin.EXTRA_PASSWORD_CONFIGURE)
                                )
                            }

                        is ExtraPasswordNavigation.Confirm -> appNavigator.navigate(
                            destination = ConfirmExtraPasswordNavItem,
                            route = ConfirmExtraPasswordNavItem.buildRoute(it.password)
                        )

                        ExtraPasswordNavigation.FinishedConfiguring -> appNavigator.popUpTo(Account)
                        is ExtraPasswordNavigation.Remove -> dismissBottomSheet {
                            appNavigator.navigate(
                                destination = Auth,
                                route = Auth.buildRoute(AuthOrigin.EXTRA_PASSWORD_REMOVE)
                            )
                        }
                    }
                }
            )
        }
    )
    profileGraph(
        onNavigateEvent = {
            when (it) {
                ProfileNavigation.Account -> appNavigator.navigate(Account)
                ProfileNavigation.Settings -> appNavigator.navigate(Settings)
                ProfileNavigation.List -> appNavigator.popUpTo(Home)
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
                    appNavigator.navigate(
                        destination = EnterPin,
                        route = EnterPin.buildRoute(AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY)
                    )
                }

                ProfileNavigation.SecurityCenter -> appNavigator.navigate(
                    destination = SecurityCenterHomeNavItem
                )

                ProfileNavigation.SecureLinks -> appNavigator.navigate(
                    destination = SecureLinksListNavItem
                )

                is ProfileNavigation.UpsellSecureLinks -> appNavigator.navigate(
                    destination = UpsellNavItem,
                    route = UpsellNavItem.createNavRoute(paidFeature = it.paidFeature)
                )

                ProfileNavigation.OnAddAccount -> onNavigate(AppNavigation.AddAccount)
                is ProfileNavigation.OnRemoveAccount -> onNavigate(AppNavigation.RemoveAccount(it.userId))
                is ProfileNavigation.OnSignIn -> onNavigate(AppNavigation.SignIn(it.userId))
                is ProfileNavigation.OnSignOut -> onNavigate(AppNavigation.SignOut(it.userId))
                is ProfileNavigation.OnSwitchAccount -> onNavigate(AppNavigation.SwitchAccount(it.userId))
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
                SettingsNavigation.SyncDialog -> appNavigator.navigate(SyncNavItem)
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
                    is CreateLoginNavigation.LoginCreatedWithPasskey -> {
                        throw IllegalStateException("Cannot create login with passkey from main app")
                    }

                    is CreateLoginNavigation.SelectVault -> {
                        appNavigator.navigate(
                            destination = SelectVaultBottomsheet,
                            route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                        )
                    }
                }

                is BaseLoginNavigation.OnUpdateLoginEvent -> when (val event = it.event) {
                    is UpdateLoginNavigation.LoginUpdated -> {
                        if (!appNavigator.hasDestinationInStack(SecurityCenterHomeNavItem)) {
                            appNavigator.navigate(
                                destination = ViewItem,
                                route = ViewItem.createNavRoute(event.shareId, event.itemId),
                                backDestination = Home
                            )
                        } else {
                            appNavigator.navigateBack()
                        }
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

                BaseLoginNavigation.AddCustomField -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(AddCustomFieldBottomSheetNavItem(prefix))
                }

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(it.type),
                        backDestination = backDestination
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix)
                            .buildRoute(it.index, it.currentValue)
                    )
                }

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            it.index,
                            it.currentValue
                        ),
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
        }
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
    createUpdateIdentityGraph(
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateIdentity) -> CreateIdentity
                appNavigator.hasDestinationInStack(UpdateIdentity) -> UpdateIdentity
                else -> null
            }
            when (it) {
                BaseIdentityNavigation.Close -> dismissBottomSheet { appNavigator.navigateBack() }
                is BaseIdentityNavigation.OpenExtraFieldBottomSheet -> appNavigator.navigate(
                    destination = IdentityFieldsBottomSheet,
                    route = IdentityFieldsBottomSheet.createRoute(
                        it.addIdentityFieldType,
                        it.sectionIndex
                    )
                )

                is CreateIdentityNavigation.ItemCreated -> appNavigator.navigateBack()
                is CreateIdentityNavigation.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                )

                BaseIdentityNavigation.OpenCustomFieldBottomSheet -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(AddCustomFieldBottomSheetNavItem(prefix))
                }

                is BaseIdentityNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(it.type),
                        backDestination = backDestination
                    )
                }

                is BaseIdentityNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            it.index,
                            it.title
                        ),
                        backDestination = backDestination
                    )
                }

                is BaseIdentityNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                            it.index,
                            it.title
                        )
                    )
                }

                BaseIdentityNavigation.RemovedCustomField -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                BaseIdentityNavigation.AddExtraSection ->
                    appNavigator.navigate(CustomSectionNameDialogNavItem)

                is BaseIdentityNavigation.EditCustomSection -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = EditCustomSectionNameDialogNavItem,
                        route = EditCustomSectionNameDialogNavItem.buildRoute(it.index, it.title),
                        backDestination = backDestination
                    )
                }

                is BaseIdentityNavigation.ExtraSectionOptions -> appNavigator.navigate(
                    destination = CustomSectionOptionsBottomSheetNavItem,
                    route = CustomSectionOptionsBottomSheetNavItem.buildRoute(it.index, it.title)
                )

                BaseIdentityNavigation.RemoveCustomSection -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                is UpdateIdentityNavigation.IdentityUpdated -> appNavigator.navigate(
                    destination = ItemDetailsNavItem,
                    route = ItemDetailsNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    ),
                    backDestination = Home
                )
            }
        }
    )
    itemDetailGraph(
        onNavigate = {
            when (it) {
                ItemDetailNavigation.Back -> {
                    appNavigator.navigateBack()
                }

                ItemDetailNavigation.CloseBottomSheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
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
                        is ItemContents.Identity -> {
                            // Not required for identity as already migrated to new item-details feature
                            throw IllegalStateException("Identity should navigate from new graph")
                        }
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

                        is ItemContents.Identity -> {
                            // Not required for identity as already migrated to new item-details feature
                            throw IllegalStateException("Identity should navigate from new graph")
                        }

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
                        route = ManageVault.createRoute(it.shareId),
                        backDestination = ViewItem
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

                is ItemDetailNavigation.ViewPasskeyDetails -> {
                    appNavigator.navigate(
                        destination = ViewPasskeyDetailsBottomSheet,
                        route = ViewPasskeyDetailsBottomSheet.buildRoute(
                            shareId = it.shareId,
                            itemId = it.itemId,
                            passkeyId = it.passkeyId
                        )
                    )
                }

                is ItemDetailNavigation.OnViewItemHistory -> appNavigator.navigate(
                    destination = ItemHistoryTimelineNavItem,
                    route = ItemHistoryTimelineNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )

                is ItemDetailNavigation.ViewReusedPasswords -> appNavigator.navigate(
                    destination = LoginItemDetailsReusedPassNavItem,
                    route = LoginItemDetailsReusedPassNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )
            }
        }
    )

    itemDetailsNavGraph(
        onNavigated = { itemDetailsNavDestination ->
            when (itemDetailsNavDestination) {
                ItemDetailsNavDestination.Back -> appNavigator.navigateBack(
                    comesFromBottomsheet = false
                )

                ItemDetailsNavDestination.Home -> dismissBottomSheet {
                    appNavigator.popUpTo(destination = Home)
                }

                is ItemDetailsNavDestination.EditItem -> appNavigator.navigate(
                    destination = getItemDetailsDestination(itemDetailsNavDestination.itemCategory),
                    route = getItemDetailsRoute(
                        itemCategory = itemDetailsNavDestination.itemCategory,
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.PasskeyDetails -> appNavigator.navigate(
                    destination = ViewPasskeyDetailsBottomSheet,
                    route = ViewPasskeyDetailsBottomSheet.buildRoute(
                        passkey = itemDetailsNavDestination.passkeyContent
                    )
                )

                is ItemDetailsNavDestination.ItemHistory -> appNavigator.navigate(
                    destination = ItemHistoryTimelineNavItem,
                    route = ItemHistoryTimelineNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.ItemSharing -> appNavigator.navigate(
                    destination = ShareFromItem,
                    route = ShareFromItem.buildRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.ManageSharedVault -> appNavigator.navigate(
                    destination = ManageVault,
                    route = ManageVault.createRoute(
                        shareId = itemDetailsNavDestination.sharedVaultId
                    ),
                    backDestination = getItemDetailsDestination(itemDetailsNavDestination.itemCategory)
                )

                is ItemDetailsNavDestination.ItemMenu -> appNavigator.navigate(
                    destination = ItemDetailsMenuNavItem,
                    route = ItemDetailsMenuNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                ItemDetailsNavDestination.ItemMigration -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateSelectVault,
                        route = MigrateSelectVault.createNavRouteForMigrateSelectedItems(
                            filter = MigrateVaultFilter.All
                        )
                    )
                }

                ItemDetailsNavDestination.DismissBottomSheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }
            }
        }
    )

    itemHistoryNavGraph(
        onNavigated = { itemHistoryNavDestination ->
            when (itemHistoryNavDestination) {
                ItemHistoryNavDestination.Back -> appNavigator.navigateBack(
                    comesFromBottomsheet = false
                )

                is ItemHistoryNavDestination.Detail -> appNavigator.popUpTo(
                    destination = getItemDetailsDestination(itemHistoryNavDestination.itemCategory),
                    comesFromBottomsheet = false
                )

                is ItemHistoryNavDestination.Restore -> appNavigator.navigate(
                    destination = ItemHistoryRestoreNavItem,
                    route = ItemHistoryRestoreNavItem.createNavRoute(
                        shareId = itemHistoryNavDestination.shareId,
                        itemId = itemHistoryNavDestination.itemId,
                        itemRevision = itemHistoryNavDestination.itemRevision
                    )
                )

                is ItemHistoryNavDestination.Timeline -> appNavigator.navigate(
                    destination = ItemHistoryTimelineNavItem,
                    route = ItemHistoryTimelineNavItem.createNavRoute(
                        shareId = itemHistoryNavDestination.shareId,
                        itemId = itemHistoryNavDestination.itemId
                    )
                )

                is ItemHistoryNavDestination.PasskeyDetail -> appNavigator.navigate(
                    destination = ViewPasskeyDetailsBottomSheet,
                    route = ViewPasskeyDetailsBottomSheet.buildRoute(
                        passkey = itemHistoryNavDestination.passkey
                    )
                )
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
        navigation = {
            when (it) {
                is AuthNavigation.Back -> when (it.origin) {
                    AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY,
                    AuthOrigin.EXTRA_PASSWORD_REMOVE,
                    AuthOrigin.EXTRA_PASSWORD_CONFIGURE -> appNavigator.navigateBack()

                    AuthOrigin.AUTO_LOCK,
                    AuthOrigin.EXTRA_PASSWORD_LOGIN -> onNavigate(AppNavigation.Finish)
                }

                is AuthNavigation.Success -> dismissBottomSheet {
                    when (it.origin) {
                        AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY ->
                            appNavigator.navigateBackWithResult(
                                key = ENTER_PIN_PARAMETER_KEY,
                                value = true
                            )

                        AuthOrigin.EXTRA_PASSWORD_CONFIGURE ->
                            appNavigator.navigate(
                                destination = SetExtraPasswordNavItem,
                                backDestination = Account
                            )

                        AuthOrigin.AUTO_LOCK -> appNavigator.navigateBack()
                        AuthOrigin.EXTRA_PASSWORD_LOGIN -> {}
                        AuthOrigin.EXTRA_PASSWORD_REMOVE -> appNavigator.navigateBackWithResult(
                            key = ENTER_PIN_PARAMETER_KEY,
                            value = true
                        )
                    }
                }

                AuthNavigation.Dismissed -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Failed -> appNavigator.navigateBack()
                AuthNavigation.SignOut -> onNavigate(AppNavigation.SignOut())
                AuthNavigation.ForceSignOut -> onNavigate(AppNavigation.ForceSignOut)
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )
            }
        }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.popUpTo(Home) },
        onNavigateBack = { onNavigate(AppNavigation.Finish) }
    )
    featureFlagsGraph()
    trialGraph {
        when (it) {
            TrialNavigation.Close -> appNavigator.navigateBack()
            TrialNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }

    securityCenterNavGraph(
        onNavigated = { destination ->
            when (destination) {
                is SecurityCenterNavDestination.Back -> dismissBottomSheet {
                    appNavigator.navigateBack(
                        comesFromBottomsheet = destination.comesFromBottomSheet,
                        force = destination.force
                    )
                }

                SecurityCenterNavDestination.Home -> appNavigator.navigate(
                    destination = SecurityCenterHomeNavItem
                )

                SecurityCenterNavDestination.MainHome -> appNavigator.navigate(
                    destination = Home
                )

                is SecurityCenterNavDestination.ItemDetails -> appNavigator.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(
                        shareId = destination.shareId,
                        itemId = destination.itemId,
                        scope = when (destination.origin) {
                            Origin.Excluded -> ItemDetailNavScope.MonitorExcluded
                            Origin.Missing2fa -> ItemDetailNavScope.MonitorMissing2fa
                            Origin.Report -> ItemDetailNavScope.MonitorReport
                            Origin.ReusedPassword -> ItemDetailNavScope.MonitorReusedPassword
                            Origin.WeakPasswords -> ItemDetailNavScope.MonitorWeakPassword
                        }
                    )
                )

                SecurityCenterNavDestination.MainNewItem -> appNavigator.navigate(
                    destination = CreateItemBottomsheet
                )

                SecurityCenterNavDestination.MainProfile -> appNavigator.navigate(
                    destination = Profile
                )

                SecurityCenterNavDestination.DarkWebMonitoring -> appNavigator.navigate(
                    destination = DarkWebMonitorNavItem
                )

                SecurityCenterNavDestination.ReusedPasswords -> appNavigator.navigate(
                    destination = SecurityCenterReusedPassNavItem
                )

                SecurityCenterNavDestination.WeakPasswords -> appNavigator.navigate(
                    destination = SecurityCenterWeakPassNavItem
                )

                SecurityCenterNavDestination.MissingTFA -> appNavigator.navigate(
                    destination = SecurityCenterMissingTFANavItem
                )

                SecurityCenterNavDestination.Empty -> appNavigator.navigateBack(force = true)

                SecurityCenterNavDestination.Sentinel -> appNavigator.navigate(
                    destination = SecurityCenterSentinelNavItem
                )

                SecurityCenterNavDestination.DarkWebMonitor -> appNavigator.navigate(
                    destination = DarkWebMonitorNavItem
                )

                is SecurityCenterNavDestination.Upsell -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(paidFeature = destination.paidFeature)
                    )
                }

                is SecurityCenterNavDestination.VerifyEmail -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SecurityCenterVerifyEmailNavItem,
                        route = SecurityCenterVerifyEmailNavItem.createNavRoute(
                            id = destination.id,
                            email = destination.email
                        )
                    )
                }

                is SecurityCenterNavDestination.AddCustomEmail -> appNavigator.navigate(
                    destination = SecurityCenterCustomEmailNavItem,
                    route = SecurityCenterCustomEmailNavItem.buildRoute(
                        email = destination.email
                    )
                )


                SecurityCenterNavDestination.EmailVerified -> appNavigator.popUpTo(
                    destination = DarkWebMonitorNavItem
                )

                is SecurityCenterNavDestination.CustomEmailReport -> appNavigator.navigate(
                    destination = SecurityCenterCustomEmailReportNavItem,
                    route = SecurityCenterCustomEmailReportNavItem.createNavRoute(
                        id = destination.id,
                        email = destination.email,
                        breachCount = destination.breachCount
                    )
                )

                is SecurityCenterNavDestination.AliasEmailReport -> appNavigator.navigate(
                    destination = SecurityCenterAliasEmailReportNavItem,
                    route = SecurityCenterAliasEmailReportNavItem.createNavRoute(
                        id = destination.id,
                        email = destination.email,
                        breachCount = destination.breachCount
                    )
                )

                is SecurityCenterNavDestination.ProtonEmailReport -> appNavigator.navigate(
                    destination = SecurityCenterProtonEmailReportNavItem,
                    route = SecurityCenterProtonEmailReportNavItem.createNavRoute(
                        id = destination.id,
                        email = destination.email,
                        breachCount = destination.breachCount
                    )
                )

                is SecurityCenterNavDestination.UnverifiedEmailOptions -> appNavigator.navigate(
                    destination = CustomEmailOptionsNavItem,
                    route = CustomEmailOptionsNavItem.buildRoute(
                        breachEmailId = destination.id,
                        customEmail = destination.email
                    )
                )

                is SecurityCenterNavDestination.CustomEmailBreachDetail ->
                    appNavigator.navigate(
                        destination = SecurityCenterCustomEmailBreachDetailNavItem,
                        route = SecurityCenterCustomEmailBreachDetailNavItem.createNavRoute(
                            id = destination.id
                        )
                    )

                is SecurityCenterNavDestination.AliasEmailBreachDetail -> appNavigator.navigate(
                    destination = SecurityCenterAliasEmailBreachDetailNavItem,
                    route = SecurityCenterAliasEmailBreachDetailNavItem.createNavRoute(
                        id = destination.id
                    )
                )

                SecurityCenterNavDestination.ExcludedItems -> appNavigator.navigate(
                    destination = SecurityCenterExcludedItemsNavItem
                )

                is SecurityCenterNavDestination.ProtonEmailBreachDetail -> appNavigator.navigate(
                    destination = SecurityCenterProtonEmailBreachDetailNavItem,
                    route = SecurityCenterProtonEmailBreachDetailNavItem.createNavRoute(
                        id = destination.id
                    )
                )

                SecurityCenterNavDestination.AllProtonEmails -> appNavigator.navigate(
                    destination = SecurityCenterProtonListNavItem
                )

                SecurityCenterNavDestination.AllAliasEmails -> appNavigator.navigate(
                    destination = SecurityCenterAliasListNavItem
                )

                is SecurityCenterNavDestination.GlobalMonitorAddressOptions -> appNavigator.navigate(
                    destination = SecurityCenterGlobalAddressOptionsNavItem,
                    route = SecurityCenterGlobalAddressOptionsNavItem.createNavRoute(
                        addressOptionsType = destination.addressOptionsType,
                        globalMonitorAddressType = destination.globalMonitorAddressType
                    )
                )

                is SecurityCenterNavDestination.DarkWebHelp -> appNavigator.navigate(
                    destination = DarkWebHelpNavItem,
                    route = DarkWebHelpNavItem.createRoute(
                        titleResId = destination.titleResId,
                        textResId = destination.textResId
                    )
                )

                SecurityCenterNavDestination.CannotAddCustomEmails ->
                    appNavigator.navigate(DarkWebCannotAddCustomEmailNavItem)

                is SecurityCenterNavDestination.ReportAliasAddressOptions -> appNavigator.navigate(
                    destination = SecurityCenterAliasAddressOptionsNavItem,
                    route = SecurityCenterAliasAddressOptionsNavItem.createNavRoute(
                        id = destination.breachEmailId,
                        addressOptionsType = destination.addressOptionsType
                    )
                )

                is SecurityCenterNavDestination.ReportCustomAddressOptions -> appNavigator.navigate(
                    destination = SecurityCenterCustomAddressOptionsNavItem,
                    route = SecurityCenterCustomAddressOptionsNavItem.createNavRoute(
                        id = destination.breachEmailId,
                        addressOptionsType = destination.addressOptionsType
                    )
                )

                is SecurityCenterNavDestination.ReportProtonAddressOptions -> appNavigator.navigate(
                    destination = SecurityCenterProtonAddressOptionsNavItem,
                    route = SecurityCenterProtonAddressOptionsNavItem.createNavRoute(
                        id = destination.breachEmailId,
                        addressOptionsType = destination.addressOptionsType
                    )
                )

                SecurityCenterNavDestination.BackToDarkWebMonitoring -> dismissBottomSheet {
                    appNavigator.popUpTo(DarkWebMonitorNavItem)
                }
            }
        }
    )

    sharingGraph {
        when (it) {
            SharingNavigation.Back -> appNavigator.navigateBack()

            SharingNavigation.BackToHome -> dismissBottomSheet {
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
                route = SharingPermissions.createRoute(shareId = it.shareId)
            )

            is SharingNavigation.Summary -> appNavigator.navigate(
                destination = SharingSummary,
                route = SharingSummary.createRoute(shareId = it.shareId)
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

            is SharingNavigation.InviteToVaultEditPermissions -> appNavigator.navigate(
                destination = SharingEditPermissions,
                route = SharingEditPermissions.buildRouteForEditOne(
                    email = it.email,
                    permission = it.permission.toSharingType()
                )
            )

            is SharingNavigation.InviteToVaultEditAllPermissions -> appNavigator.navigate(
                destination = SharingEditPermissions,
                route = SharingEditPermissions.buildRouteForEditAll()
            )

            SharingNavigation.InviteError -> appNavigator.navigate(
                destination = InvitesErrorDialog
            )

            is SharingNavigation.ShareItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = SecureLinksCreateNavItem,
                    route = SecureLinksCreateNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )
            }

            is SharingNavigation.Upsell -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = UpsellNavItem,
                    route = UpsellNavItem.createNavRoute(paidFeature = it.paidFeature)
                )
            }

            is SharingNavigation.ManageSharedVault -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = ManageVault,
                    route = ManageVault.createRoute(shareId = it.sharedVaultId)
                )
            }
        }
    }

    syncNavGraph { destination ->
        when (destination) {
            SyncNavDestination.Back -> appNavigator.navigateBack()
        }
    }

    upsellNavGraph(
        onNavigated = { upsellNavDestination ->
            when (upsellNavDestination) {
                UpsellNavDestination.Back -> appNavigator.navigateBack(comesFromBottomsheet = false)
                UpsellNavDestination.Upgrade -> onNavigate(AppNavigation.Upgrade)
                UpsellNavDestination.Subscription -> onNavigate(AppNavigation.Subscription)
            }
        }
    )

    secureLinksNavGraph(
        onNavigated = { destination ->
            when (destination) {
                SecureLinksNavDestination.Back -> appNavigator.navigateBack(
                    comesFromBottomsheet = false
                )

                is SecureLinksNavDestination.Close -> appNavigator.popUpTo(
                    destination = getItemDetailsDestination(destination.itemCategory)
                )

                SecureLinksNavDestination.DismissBottomSheet -> dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }

                SecureLinksNavDestination.Profile -> appNavigator.navigate(
                    destination = Profile
                )

                is SecureLinksNavDestination.SecureLinkOverview -> when (destination.scope) {
                    SecureLinksOverviewNavScope.SecureLinksGeneration -> appNavigator.navigate(
                        destination = SecureLinksOverviewScreenNavItem,
                        route = SecureLinksOverviewScreenNavItem.createNavRoute(destination.secureLinkId),
                        backDestination = ViewItem
                    )

                    SecureLinksOverviewNavScope.SecureLinksList -> appNavigator.navigate(
                        destination = SecureLinksOverviewBottomSheetNavItem,
                        route = SecureLinksOverviewBottomSheetNavItem.createNavRoute(destination.secureLinkId)
                    )
                }

                SecureLinksNavDestination.SecureLinksList -> appNavigator.navigate(
                    destination = SecureLinksListNavItem
                )

                is SecureLinksNavDestination.SecureLinksListMenu -> appNavigator.navigate(
                    destination = SecureLinksListMenuNavItem,
                    route = SecureLinksListMenuNavItem.createNavRoute(destination.secureLinkId)
                )
            }
        }
    )
}

// This fun should be removed once all categories are migrated to new item-details feature
// ItemDetailsNavItem should be keep as new destination
private fun getItemDetailsDestination(itemCategory: ItemCategory) = when (itemCategory) {
    ItemCategory.Login,
    ItemCategory.Alias,
    ItemCategory.Note,
    ItemCategory.CreditCard -> ViewItem

    // Identity is the first item category migrated
    ItemCategory.Identity -> ItemDetailsNavItem

    ItemCategory.Unknown,
    ItemCategory.Password -> throw IllegalArgumentException("Cannot view items with category: $itemCategory")
}

// This fun should be removed once all categories are migrated to new item-details feature
// ItemDetailsNavItem route should be keep as new destination route
private fun getItemDetailsRoute(
    itemCategory: ItemCategory,
    shareId: ShareId,
    itemId: ItemId
) = when (itemCategory) {
    ItemCategory.Login,
    ItemCategory.Alias,
    ItemCategory.Note,
    ItemCategory.CreditCard -> ViewItem.createNavRoute(shareId, itemId)

    // Identity is the first item category migrated
    ItemCategory.Identity -> ItemDetailsNavItem.createNavRoute(shareId, itemId)

    ItemCategory.Unknown,
    ItemCategory.Password -> throw IllegalArgumentException("Cannot view items with category: $itemCategory")
}
