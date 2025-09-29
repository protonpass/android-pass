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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.commonuimodels.api.items.ItemDetailNavScope
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.account.Account
import proton.android.pass.features.account.AccountNavigation
import proton.android.pass.features.account.accountGraph
import proton.android.pass.features.alias.contacts.AliasContactsNavigation
import proton.android.pass.features.alias.contacts.aliasContactGraph
import proton.android.pass.features.alias.contacts.create.navigation.CreateAliasContactNavItem
import proton.android.pass.features.alias.contacts.detail.navigation.DetailAliasContactNavItem
import proton.android.pass.features.alias.contacts.onboarding.navigation.OnBoardingAliasContactNavItem
import proton.android.pass.features.alias.contacts.options.navigation.OptionsAliasContactNavItem
import proton.android.pass.features.attachments.AttachmentsNavigation
import proton.android.pass.features.attachments.addattachment.navigation.AddAttachmentNavItem
import proton.android.pass.features.attachments.attachmentoptionsondetail.navigation.AttachmentOptionsOnDetailNavItem
import proton.android.pass.features.attachments.attachmentoptionsonedit.navigation.AttachmentOptionsOnEditNavItem
import proton.android.pass.features.attachments.attachmentsGraph
import proton.android.pass.features.attachments.camera.navigation.CameraNavItem
import proton.android.pass.features.attachments.deleteall.navigation.DeleteAllAttachmentsDialogNavItem
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavItem
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavItem
import proton.android.pass.features.attachments.renameattachment.navigation.RenameAttachmentNavItem
import proton.android.pass.features.attachments.storagefull.navigation.StorageFullNavItem
import proton.android.pass.features.auth.Auth
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.AuthOrigin
import proton.android.pass.features.auth.EnterPin
import proton.android.pass.features.auth.authGraph
import proton.android.pass.features.extrapassword.ExtraPasswordNavigation
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordNavItem
import proton.android.pass.features.extrapassword.confirm.navigation.ConfirmExtraPasswordNavItem
import proton.android.pass.features.extrapassword.extraPasswordGraph
import proton.android.pass.features.extrapassword.infosheet.navigation.ExtraPasswordInfoNavItem
import proton.android.pass.features.extrapassword.options.navigation.ExtraPasswordOptionsNavItem
import proton.android.pass.features.featureflags.FeatureFlagRoute
import proton.android.pass.features.featureflags.featureFlagsGraph
import proton.android.pass.features.home.HOME_ENABLE_BULK_ACTIONS_KEY
import proton.android.pass.features.home.HomeNavItem
import proton.android.pass.features.home.HomeNavigation
import proton.android.pass.features.home.HomeUpgradeDialog
import proton.android.pass.features.home.homeGraph
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageModalDestination
import proton.android.pass.features.inappmessages.bottomsheet.navigation.inAppMessageGraph
import proton.android.pass.features.item.details.detail.navigation.ItemDetailsNavItem
import proton.android.pass.features.item.details.detailforbidden.navigation.ItemDetailsForbiddenNavItem
import proton.android.pass.features.item.details.detailleave.navigation.ItemDetailsLeaveNavItem
import proton.android.pass.features.item.details.detailmenu.navigation.ItemDetailsMenuNavItem
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.ViewPasskeyDetailsBottomSheet
import proton.android.pass.features.item.details.qrviewer.navigation.QRViewerNavItem
import proton.android.pass.features.item.details.reusedpass.navigation.LoginItemDetailsReusedPassNavItem
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination
import proton.android.pass.features.item.details.shared.navigation.itemDetailsNavGraph
import proton.android.pass.features.item.history.confirmreset.navigation.ConfirmResetHistoryDialogNavItem
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.navigation.itemHistoryNavGraph
import proton.android.pass.features.item.history.options.navigation.HistoryOptionsNavItem
import proton.android.pass.features.item.history.restore.navigation.ItemHistoryRestoreNavItem
import proton.android.pass.features.item.history.timeline.navigation.ItemHistoryTimelineNavItem
import proton.android.pass.features.item.options.aliases.trash.dialogs.navigation.ItemOptionsAliasTrashDialogNavItem
import proton.android.pass.features.item.options.shared.navigation.ItemOptionsNavDestination
import proton.android.pass.features.item.options.shared.navigation.itemOptionsNavGraph
import proton.android.pass.features.item.trash.shared.navigation.ItemTrashNavDestination
import proton.android.pass.features.item.trash.shared.navigation.itemTrashNavGraph
import proton.android.pass.features.item.trash.trashdelete.navigation.ItemTrashDeleteNavItem
import proton.android.pass.features.item.trash.trashmenu.navigation.ItemTrashMenuNavItem
import proton.android.pass.features.itemcreate.alias.AliasSelectMailboxBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.AliasSelectSuffixBottomSheetNavItem
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.alias.CreateAliasNavItem
import proton.android.pass.features.itemcreate.alias.CreateAliasNavigation
import proton.android.pass.features.itemcreate.alias.EditAliasNavItem
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation
import proton.android.pass.features.itemcreate.alias.createUpdateCreditCardGraph
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomSheetMode
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation
import proton.android.pass.features.itemcreate.bottomsheets.createitem.bottomsheetCreateItemGraph
import proton.android.pass.features.itemcreate.bottomsheets.customfield.AddCustomFieldBottomSheetNavItem
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionNameDialogNavItem
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionOptionsBottomSheetNavItem
import proton.android.pass.features.itemcreate.common.customsection.EditCustomSectionNameDialogNavItem
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation
import proton.android.pass.features.itemcreate.creditcard.CreateCreditCardNavItem
import proton.android.pass.features.itemcreate.creditcard.CreateCreditCardNavigation
import proton.android.pass.features.itemcreate.creditcard.EditCreditCardNavItem
import proton.android.pass.features.itemcreate.creditcard.UpdateCreditCardNavigation
import proton.android.pass.features.itemcreate.creditcard.createUpdateAliasGraph
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.BaseCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.CreateCustomItemNavItem
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.CreateCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.UpdateCustomItemNavItem
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.UpdateCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.createUpdateCustomItemGraph
import proton.android.pass.features.itemcreate.custom.selecttemplate.navigation.SelectTemplateNavItem
import proton.android.pass.features.itemcreate.custom.selecttemplate.navigation.SelectTemplateNavigation
import proton.android.pass.features.itemcreate.custom.selecttemplate.navigation.selectTemplateGraph
import proton.android.pass.features.itemcreate.custom.selectwifisecuritytype.navigation.SelectWifiSecurityTypeNavItem
import proton.android.pass.features.itemcreate.custom.selectwifisecuritytype.navigation.WIFI_SECURITY_TYPE_PARAMETER_KEY
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.dialogs.customfield.EditCustomFieldNameDialogNavItem
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation
import proton.android.pass.features.itemcreate.identity.navigation.CreateIdentityNavItem
import proton.android.pass.features.itemcreate.identity.navigation.CreateIdentityNavigation
import proton.android.pass.features.itemcreate.identity.navigation.UpdateIdentityNavItem
import proton.android.pass.features.itemcreate.identity.navigation.UpdateIdentityNavigation
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.IdentityFieldsBottomSheet
import proton.android.pass.features.itemcreate.identity.navigation.createUpdateIdentityGraph
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.Companion.toIndex
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.CreateLoginNavigation
import proton.android.pass.features.itemcreate.login.EditLoginNavItem
import proton.android.pass.features.itemcreate.login.UpdateLoginNavigation
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.AliasOptionsBottomSheet
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.createUpdateLoginGraph
import proton.android.pass.features.itemcreate.note.BaseNoteNavigation
import proton.android.pass.features.itemcreate.note.CreateNoteNavItem
import proton.android.pass.features.itemcreate.note.CreateNoteNavigation
import proton.android.pass.features.itemcreate.note.UpdateNoteNavItem
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation
import proton.android.pass.features.itemcreate.note.createUpdateNoteGraph
import proton.android.pass.features.itemcreate.totp.CameraTotpNavItem
import proton.android.pass.features.itemcreate.totp.PhotoPickerTotpNavItem
import proton.android.pass.features.migrate.MigrateConfirmVault
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateNavigation
import proton.android.pass.features.migrate.MigrateSelectVault
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.migrateGraph
import proton.android.pass.features.migrate.warningshared.navigation.MigrateSharedWarningNavItem
import proton.android.pass.features.onboarding.OnBoarding
import proton.android.pass.features.onboarding.onBoardingGraph
import proton.android.pass.features.password.GeneratePasswordBottomsheet
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.features.password.GeneratePasswordNavigation
import proton.android.pass.features.password.dialog.mode.PasswordModeDialog
import proton.android.pass.features.password.dialog.separator.WordSeparatorDialog
import proton.android.pass.features.password.generatePasswordBottomsheetGraph
import proton.android.pass.features.profile.AppLockTimeBottomsheet
import proton.android.pass.features.profile.AppLockTypeBottomsheet
import proton.android.pass.features.profile.ENTER_PIN_PARAMETER_KEY
import proton.android.pass.features.profile.FeedbackBottomsheet
import proton.android.pass.features.profile.PinConfig
import proton.android.pass.features.profile.ProfileNavItem
import proton.android.pass.features.profile.ProfileNavigation
import proton.android.pass.features.profile.manageaccountconfirmation.navigation.ManageAccountConfirmationNavItem
import proton.android.pass.features.profile.profileGraph
import proton.android.pass.features.report.navigation.ReportNavDestination
import proton.android.pass.features.report.navigation.ReportNavItem
import proton.android.pass.features.report.navigation.reportNavGraph
import proton.android.pass.features.searchoptions.FilterBottomsheetNavItem
import proton.android.pass.features.searchoptions.SearchOptionsBottomsheetNavItem
import proton.android.pass.features.searchoptions.SearchOptionsNavigation
import proton.android.pass.features.searchoptions.SortingBottomsheetNavItem
import proton.android.pass.features.searchoptions.SortingLocation
import proton.android.pass.features.searchoptions.searchOptionsGraph
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
import proton.android.pass.features.settings.ClearClipboardOptions
import proton.android.pass.features.settings.ClipboardSettings
import proton.android.pass.features.settings.LogView
import proton.android.pass.features.settings.Settings
import proton.android.pass.features.settings.SettingsNavigation
import proton.android.pass.features.settings.ThemeSelector
import proton.android.pass.features.settings.settingsGraph
import proton.android.pass.features.sharing.AcceptInvite
import proton.android.pass.features.sharing.InvitesErrorDialog
import proton.android.pass.features.sharing.InvitesInfoDialog
import proton.android.pass.features.sharing.ManageVault
import proton.android.pass.features.sharing.REFRESH_MEMBER_LIST_FLAG
import proton.android.pass.features.sharing.ShareFromItem
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.features.sharing.SharingPermissions
import proton.android.pass.features.sharing.SharingSummary
import proton.android.pass.features.sharing.SharingWith
import proton.android.pass.features.sharing.extensions.toSharingType
import proton.android.pass.features.sharing.manage.bottomsheet.ConfirmTransferOwnership
import proton.android.pass.features.sharing.manage.bottomsheet.InviteOptionsBottomSheet
import proton.android.pass.features.sharing.manage.bottomsheet.InviteTypeValue
import proton.android.pass.features.sharing.manage.bottomsheet.MemberOptionsBottomSheet
import proton.android.pass.features.sharing.manage.item.navigation.ManageItemNavItem
import proton.android.pass.features.sharing.manage.iteminviteoptions.navigation.ManageItemInviteOptionsNavItem
import proton.android.pass.features.sharing.manage.itemmemberoptions.navigation.ManageItemMemberOptionsNavItem
import proton.android.pass.features.sharing.sharingGraph
import proton.android.pass.features.sharing.sharingpermissions.bottomsheet.SharingEditPermissions
import proton.android.pass.features.sl.sync.details.navigation.SimpleLoginSyncDetailsNavItem
import proton.android.pass.features.sl.sync.domains.select.navigation.SimpleLoginSyncDomainSelectNavItem
import proton.android.pass.features.sl.sync.mailboxes.change.navigation.SimpleLoginSyncMailboxChangeNavItem
import proton.android.pass.features.sl.sync.mailboxes.create.navigation.SimpleLoginSyncMailboxCreateNavItem
import proton.android.pass.features.sl.sync.mailboxes.delete.navigation.SimpleLoginSyncMailboxDeleteNavItem
import proton.android.pass.features.sl.sync.mailboxes.options.navigation.SimpleLoginSyncMailboxOptionsNavItem
import proton.android.pass.features.sl.sync.mailboxes.verify.navigation.SimpleLoginSyncMailboxVerifyNavItem
import proton.android.pass.features.sl.sync.management.navigation.SimpleLoginSyncManagementNavItem
import proton.android.pass.features.sl.sync.settings.navigation.SimpleLoginSyncSettingsNavItem
import proton.android.pass.features.sl.sync.shared.navigation.SimpleLoginSyncNavDestination
import proton.android.pass.features.sl.sync.shared.navigation.simpleLoginSyncNavGraph
import proton.android.pass.features.sync.navigation.SyncNavDestination
import proton.android.pass.features.sync.navigation.SyncNavItem
import proton.android.pass.features.sync.navigation.syncNavGraph
import proton.android.pass.features.trial.TrialNavigation
import proton.android.pass.features.trial.TrialScreen
import proton.android.pass.features.trial.trialGraph
import proton.android.pass.features.upsell.navigation.UpsellNavDestination
import proton.android.pass.features.upsell.navigation.UpsellNavItem
import proton.android.pass.features.upsell.navigation.upsellNavGraph
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.CreateVaultNextAction
import proton.android.pass.features.vault.bottomsheet.CreateVaultScreen
import proton.android.pass.features.vault.bottomsheet.EditVaultScreen
import proton.android.pass.features.vault.bottomsheet.options.VaultOptionsBottomSheet
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.delete.DeleteVaultDialog
import proton.android.pass.features.vault.leave.LeaveVaultDialog
import proton.android.pass.features.vault.vaultGraph
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.CommonNavArgKey
import proton.android.pass.ui.AppNavigation
import proton.android.pass.ui.navigation.account.AccountRedirectsDestination
import proton.android.pass.ui.navigation.account.accountRedirectsGraph

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
                            CreateItemBottomsheetNavItem to CreateItemBottomsheetNavItem.createNavRoute(
                                mode = CreateItemBottomSheetMode.HomeFull,
                                shareId = it.shareId
                            )

                        ItemTypeUiState.Login -> CreateLoginNavItem to CreateLoginNavItem.createNavRoute(
                            it.shareId
                        )

                        ItemTypeUiState.Note -> CreateNoteNavItem to CreateNoteNavItem.createNavRoute(it.shareId)
                        ItemTypeUiState.Alias -> CreateAliasNavItem to CreateAliasNavItem.createNavRoute(it.shareId)
                        ItemTypeUiState.Password ->
                            GeneratePasswordBottomsheet to GeneratePasswordBottomsheet.buildRoute(
                                mode = GeneratePasswordBottomsheetModeValue.CopyAndClose
                            )

                        ItemTypeUiState.CreditCard ->
                            CreateCreditCardNavItem to CreateCreditCardNavItem.createNavRoute(it.shareId)

                        ItemTypeUiState.Identity ->
                            CreateIdentityNavItem to CreateIdentityNavItem.createNavRoute(it.shareId)

                        ItemTypeUiState.Custom ->
                            SelectTemplateNavItem to SelectTemplateNavItem.createNavRoute(it.shareId)
                    }

                    appNavigator.navigate(destination, route)
                }

                HomeNavigation.CloseScreen -> appNavigator.navigateBack()

                HomeNavigation.CreateVault -> {
                    appNavigator.navigate(
                        destination = CreateVaultScreen,
                        route = CreateVaultScreen.buildRoute(CreateVaultNextAction.Done)
                    )
                }

                is HomeNavigation.EditAlias -> {
                    appNavigator.navigate(
                        EditAliasNavItem,
                        EditAliasNavItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.EditLogin -> {
                    appNavigator.navigate(
                        EditLoginNavItem,
                        EditLoginNavItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.CloneLogin -> {
                    appNavigator.navigate(
                        CreateLoginNavItem,
                        CreateLoginNavItem.createNavRoute(it.shareId.some(), it.itemId.some())
                    )
                }

                is HomeNavigation.EditNote -> {
                    appNavigator.navigate(
                        UpdateNoteNavItem,
                        UpdateNoteNavItem.createNavRoute(it.shareId, it.itemId)
                    )
                }

                is HomeNavigation.DuplicateNote -> {
                    appNavigator.navigate(
                        CreateNoteNavItem,
                        CreateNoteNavItem.createNavRoute(it.shareId.some(), it.itemId.some())
                    )
                }

                is HomeNavigation.EditCreditCard -> appNavigator.navigate(
                    EditCreditCardNavItem,
                    EditCreditCardNavItem.createNavRoute(it.shareId, it.itemId)
                )

                is HomeNavigation.DuplicateCreditCard -> appNavigator.navigate(
                    CreateCreditCardNavItem,
                    CreateCreditCardNavItem.createNavRoute(it.shareId.some(), it.itemId.some())
                )

                is HomeNavigation.EditIdentity -> appNavigator.navigate(
                    UpdateIdentityNavItem,
                    UpdateIdentityNavItem.createNavRoute(it.shareId, it.itemId)
                )

                is HomeNavigation.DuplicateIdentity -> appNavigator.navigate(
                    CreateIdentityNavItem,
                    CreateIdentityNavItem.createNavRoute(it.shareId.some(), it.itemId.some())
                )

                is HomeNavigation.EditCustomItem -> appNavigator.navigate(
                    UpdateCustomItemNavItem,
                    UpdateCustomItemNavItem.createNavRoute(it.shareId, it.itemId)
                )

                is HomeNavigation.DuplicateCustomItem -> appNavigator.navigate(
                    CreateCustomItemNavItem,
                    CreateCustomItemNavItem.createNavRoute(it.shareId.some(), it.itemId.some())
                )

                is HomeNavigation.ItemDetail -> appNavigator.navigate(
                    destination = ItemDetailsNavItem,
                    route = ItemDetailsNavItem.createNavRoute(it.shareId, it.itemId)
                )

                HomeNavigation.Profile -> {
                    appNavigator.navigate(ProfileNavItem)
                }

                is HomeNavigation.SortingBottomsheet -> {
                    appNavigator.navigate(
                        SortingBottomsheetNavItem,
                        SortingBottomsheetNavItem.createNavRoute(
                            location = SortingLocation.Home
                        )
                    )
                }

                is HomeNavigation.VaultOptions -> appNavigator.navigate(
                    VaultOptionsBottomSheet,
                    VaultOptionsBottomSheet.createNavRoute(it.shareId)
                )

                HomeNavigation.TrialInfo -> appNavigator.navigate(TrialScreen)
                is HomeNavigation.OpenInvite -> appNavigator.navigate(
                    destination = AcceptInvite,
                    route = AcceptInvite.createRoute(it.inviteToken),
                    backDestination = HomeNavItem
                )

                HomeNavigation.Finish -> onNavigate(AppNavigation.Finish)
                HomeNavigation.SyncDialog -> appNavigator.navigate(
                    destination = SyncNavItem,
                    force = true
                )

                HomeNavigation.OnBoarding -> appNavigator.navigate(
                    destination = OnBoarding,
                    force = true
                )

                is HomeNavigation.ConfirmedInvite -> appNavigator.navigate(
                    destination = AcceptInvite,
                    route = AcceptInvite.createRoute(it.inviteToken)
                )

                is HomeNavigation.SearchOptions -> appNavigator.navigate(
                    destination = SearchOptionsBottomsheetNavItem,
                    route = SearchOptionsBottomsheetNavItem.createRoute(it.bulkActionsEnabled),
                    backDestination = HomeNavItem
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

                is HomeNavigation.TrashAlias -> appNavigator.navigate(
                    destination = ItemOptionsAliasTrashDialogNavItem,
                    route = ItemOptionsAliasTrashDialogNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )

                is HomeNavigation.SLSyncSettings -> appNavigator.navigate(
                    destination = SimpleLoginSyncSettingsNavItem,
                    route = SimpleLoginSyncSettingsNavItem.createNavRoute(it.shareId)
                )

                HomeNavigation.SLAliasManagement -> appNavigator.navigate(
                    destination = SimpleLoginSyncManagementNavItem
                )

                is HomeNavigation.ShareVault -> appNavigator.navigate(
                    destination = SharingWith,
                    route = SharingWith.createRoute(
                        shareId = it.shareId,
                        showEditVault = false,
                        itemIdOption = None
                    )
                )

                is HomeNavigation.ManageVault -> appNavigator.navigate(
                    destination = ManageVault,
                    route = ManageVault.createRoute(shareId = it.shareId)
                )

                HomeNavigation.ItemsMigrationSharedWarning -> appNavigator.navigate(
                    destination = MigrateSharedWarningNavItem,
                    route = MigrateSharedWarningNavItem.createNavRoute(
                        migrateMode = MigrateModeValue.SelectedItems,
                        filter = MigrateVaultFilter.All
                    )
                )

                is HomeNavigation.LeaveItemShare -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = ItemDetailsLeaveNavItem,
                        route = ItemDetailsLeaveNavItem.createNavRoute(
                            shareId = it.shareId
                        )
                    )
                }
            }
        }
    )
    searchOptionsGraph(
        onNavigateEvent = {
            when (it) {
                SearchOptionsNavigation.ResetFilters,
                is SearchOptionsNavigation.SelectSorting -> dismissBottomSheet {}

                SearchOptionsNavigation.Filter -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = FilterBottomsheetNavItem,
                        backDestination = HomeNavItem
                    )
                }

                SearchOptionsNavigation.Sorting -> dismissBottomSheet {
                    appNavigator.navigate(
                        SortingBottomsheetNavItem,
                        SortingBottomsheetNavItem.createNavRoute(
                            location = SortingLocation.Home
                        ),
                        HomeNavItem
                    )
                }

                SearchOptionsNavigation.BulkActions -> dismissBottomSheet {
                    appNavigator.setResult(
                        mapOf(HOME_ENABLE_BULK_ACTIONS_KEY to true)
                    )
                }
            }
        }
    )
    bottomsheetCreateItemGraph(
        onNavigate = {
            dismissBottomSheet {
                when (it) {
                    is CreateItemBottomsheetNavigation.CreateAlias ->
                        appNavigator.navigate(
                            CreateAliasNavItem,
                            CreateAliasNavItem.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateLogin ->
                        appNavigator.navigate(
                            CreateLoginNavItem,
                            CreateLoginNavItem.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateNote ->
                        appNavigator.navigate(
                            CreateNoteNavItem,
                            CreateNoteNavItem.createNavRoute(it.shareId)
                        )

                    CreateItemBottomsheetNavigation.CreatePassword -> {
                        val backDestination = when {
                            appNavigator.hasDestinationInStack(ProfileNavItem) -> ProfileNavItem
                            appNavigator.hasDestinationInStack(HomeNavItem) -> HomeNavItem
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
                            CreateCreditCardNavItem,
                            CreateCreditCardNavItem.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateIdentity ->
                        appNavigator.navigate(
                            CreateIdentityNavItem,
                            CreateIdentityNavItem.createNavRoute(it.shareId)
                        )

                    is CreateItemBottomsheetNavigation.CreateCustom ->
                        appNavigator.navigate(
                            SelectTemplateNavItem,
                            SelectTemplateNavItem.createNavRoute(it.shareId)
                        )
                }
            }
        }
    )
    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.CloseScreen -> appNavigator.navigateBack()
                VaultNavigation.DismissBottomsheet -> dismissBottomSheet {}

                VaultNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                is VaultNavigation.VaultSelected -> dismissBottomSheet {
                    appNavigator.setResult(
                        mapOf(KEY_VAULT_SELECTED to it.shareId.id)
                    )
                }

                is VaultNavigation.VaultEdit -> dismissBottomSheet {
                    appNavigator.navigate(
                        EditVaultScreen,
                        EditVaultScreen.createNavRoute(it.shareId)
                    )
                }

                is VaultNavigation.VaultMigrate -> appNavigator.navigate(
                    destination = MigrateSelectVault,
                    route = MigrateSelectVault.createNavRouteForMigrateAll(
                        shareId = it.shareId
                    )
                )

                is VaultNavigation.VaultMigrateSharedWarning -> appNavigator.navigate(
                    destination = MigrateSharedWarningNavItem,
                    route = MigrateSharedWarningNavItem.createNavRoute(
                        migrateMode = MigrateModeValue.AllVaultItems,
                        shareId = it.shareId
                    )
                )

                is VaultNavigation.VaultRemove -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = DeleteVaultDialog,
                        route = DeleteVaultDialog.createNavRoute(it.shareId),
                        backDestination = HomeNavItem
                    )
                }

                is VaultNavigation.VaultShare -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SharingWith,
                        route = SharingWith.createRoute(
                            shareId = it.shareId,
                            showEditVault = it.showEditVault,
                            itemIdOption = None
                        )
                    )
                }

                is VaultNavigation.VaultLeave -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = LeaveVaultDialog,
                        route = LeaveVaultDialog.createNavRoute(it.shareId),
                        backDestination = HomeNavItem
                    )
                }

                is VaultNavigation.VaultAccess -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = ManageVault,
                        route = ManageVault.createRoute(it.shareId),
                        backDestination = HomeNavItem
                    )
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
    accountGraph(
        onNavigate = {
            when (it) {
                AccountNavigation.CloseScreen -> appNavigator.navigateBack()
                is AccountNavigation.SignOut -> onNavigate(AppNavigation.SignOut(it.userId))
                AccountNavigation.Subscription -> onNavigate(AppNavigation.Subscription)
                AccountNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                AccountNavigation.PasswordManagement -> onNavigate(AppNavigation.PasswordManagement)
                AccountNavigation.RecoveryEmail -> onNavigate(AppNavigation.RecoveryEmail)
                AccountNavigation.SetExtraPassword -> appNavigator.navigate(ExtraPasswordInfoNavItem)
                is AccountNavigation.ExtraPasswordOptions ->
                    appNavigator.navigate(ExtraPasswordOptionsNavItem)

                AccountNavigation.SecurityKeys -> onNavigate(AppNavigation.SecurityKeys)
            }
        },
        subGraph = {
            extraPasswordGraph(
                onNavigate = {
                    when (it) {
                        ExtraPasswordNavigation.CloseScreen -> appNavigator.navigateBack()
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
    selectTemplateGraph {
        when (it) {
            SelectTemplateNavigation.NavigateBack -> appNavigator.navigateBack()
            is SelectTemplateNavigation.NavigateToCreate -> appNavigator.navigate(
                destination = CreateCustomItemNavItem,
                route = CreateCustomItemNavItem.createNavRoute(
                    shareId = it.shareId,
                    templateType = it.templateType
                ),
                backDestination = SelectTemplateNavItem
            )
        }
    }
    profileGraph(
        onNavigateEvent = {
            when (it) {
                ProfileNavigation.Account -> appNavigator.navigate(Account)
                is ProfileNavigation.ManageAccountConfirmation ->
                    appNavigator.navigate(
                        ManageAccountConfirmationNavItem,
                        ManageAccountConfirmationNavItem.createNavRoute(it.userId, it.email)
                    )

                ProfileNavigation.Settings -> appNavigator.navigate(Settings)
                ProfileNavigation.Home -> appNavigator.popUpTo(HomeNavItem)
                ProfileNavigation.Feedback -> appNavigator.navigate(FeedbackBottomsheet)
                ProfileNavigation.Report -> dismissBottomSheet {
                    appNavigator.navigate(ReportNavItem)
                }

                ProfileNavigation.FeatureFlags -> appNavigator.navigate(FeatureFlagRoute)
                ProfileNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                ProfileNavigation.Finish -> onNavigate(AppNavigation.Finish)
                ProfileNavigation.CloseBottomSheet -> dismissBottomSheet {}

                ProfileNavigation.AppLockTime -> appNavigator.navigate(AppLockTimeBottomsheet)
                ProfileNavigation.AppLockType -> appNavigator.navigate(AppLockTypeBottomsheet)
                ProfileNavigation.CloseScreen -> appNavigator.navigateBack()
                ProfileNavigation.ConfigurePin -> dismissBottomSheet {
                    appNavigator.navigate(PinConfig)
                }

                ProfileNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY)
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
                ProfileNavigation.SyncDialog -> appNavigator.navigate(SyncNavItem)

                ProfileNavigation.AliasesSyncDetails -> appNavigator.navigate(
                    destination = SimpleLoginSyncDetailsNavItem
                )

                ProfileNavigation.AliasesSyncManagement -> appNavigator.navigate(
                    destination = SimpleLoginSyncManagementNavItem
                )

                is ProfileNavigation.AliasesSyncSettings -> appNavigator.navigate(
                    destination = SimpleLoginSyncSettingsNavItem,
                    route = SimpleLoginSyncSettingsNavItem.createNavRoute(it.shareId)
                )

                ProfileNavigation.StorageFull -> appNavigator.navigate(StorageFullNavItem)
            }
        }
    )
    settingsGraph(
        onNavigate = {
            when (it) {
                SettingsNavigation.SelectTheme -> appNavigator.navigate(ThemeSelector)
                SettingsNavigation.CloseScreen -> appNavigator.navigateBack()
                SettingsNavigation.DismissBottomSheet -> dismissBottomSheet {}

                SettingsNavigation.ViewLogs -> appNavigator.navigate(LogView)
                SettingsNavigation.ClipboardSettings -> dismissBottomSheet {
                    appNavigator.navigate(ClipboardSettings)
                }

                SettingsNavigation.ClearClipboardSettings -> dismissBottomSheet {
                    appNavigator.navigate(ClearClipboardOptions)
                }

                SettingsNavigation.Restart -> onNavigate(AppNavigation.Restart)
                SettingsNavigation.SyncDialog -> appNavigator.navigate(SyncNavItem)
            }
        }
    )
    createUpdateLoginGraph(
        showCreateAliasButton = true,
        canUseAttachments = true,
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
                                destination = ItemDetailsNavItem,
                                route = ItemDetailsNavItem.createNavRoute(event.shareId, event.itemId),
                                backDestination = HomeNavItem
                            )
                        } else {
                            appNavigator.navigateBack()
                        }
                    }
                }

                is BaseLoginNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(index = it.index)
                    )
                }

                BaseLoginNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)

                is BaseLoginNavigation.AliasOptions -> appNavigator.navigate(
                    destination = AliasOptionsBottomSheet,
                    route = AliasOptionsBottomSheet.createNavRoute(it.shareId, it.showUpgrade)
                )

                BaseLoginNavigation.DeleteAlias -> dismissBottomSheet {
                    appNavigator.setResult(
                        mapOf(CLEAR_ALIAS_NAV_PARAMETER_KEY to true)
                    )
                }

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
                    appNavigator.navigate(
                        destination = AddCustomFieldBottomSheetNavItem(prefix),
                        route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute(sectionIndex = None)
                    )
                }

                is BaseLoginNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(
                            type = it.type,
                            sectionIndex = None
                        ),
                        backDestination = backDestination
                    )
                }

                is BaseLoginNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentTitle = it.currentValue
                        )
                    )
                }

                is BaseLoginNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentValue = it.currentValue
                        ),
                        backDestination = backDestination
                    )
                }

                BaseLoginNavigation.RemovedCustomField -> dismissBottomSheet {}

                is BaseLoginNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromLogin(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(index = it.index),
                        backDestination = backDestination
                    )
                }

                BaseLoginNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseLoginNavigation.TotpSuccess -> appNavigator.navigateBackWithResult(it.results)
                BaseLoginNavigation.AddAttachment ->
                    appNavigator.navigate(AddAttachmentNavItem)

                is BaseLoginNavigation.OpenAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(
                            shareId = it.shareId,
                            itemId = it.itemId,
                            attachmentId = it.attachmentId
                        )
                    )

                is BaseLoginNavigation.OpenDraftAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                    )

                is BaseLoginNavigation.DeleteAllAttachments ->
                    appNavigator.navigate(
                        destination = DeleteAllAttachmentsDialogNavItem,
                        route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                    )

                BaseLoginNavigation.UpsellAttachments ->
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                    )
            }
        }
    )
    createUpdateNoteGraph(
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateNoteNavItem) -> CreateNoteNavItem
                appNavigator.hasDestinationInStack(UpdateNoteNavItem) -> UpdateNoteNavItem
                else -> null
            }
            when (it) {
                BaseNoteNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseNoteNavigation.DismissBottomsheet -> dismissBottomSheet {}
                BaseNoteNavigation.AddAttachment ->
                    appNavigator.navigate(AddAttachmentNavItem)

                is BaseNoteNavigation.OpenDraftAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                    )

                is BaseNoteNavigation.DeleteAllAttachments ->
                    appNavigator.navigate(
                        destination = DeleteAllAttachmentsDialogNavItem,
                        route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                    )

                BaseNoteNavigation.UpsellAttachments ->
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                    )

                is BaseNoteNavigation.NoteCustomFieldNavigation ->
                    when (val cevent = it.event) {
                        CustomFieldNavigation.AddCustomField -> {
                            val prefix = CustomFieldPrefix.fromNote(backDestination)
                            appNavigator.navigate(
                                destination = AddCustomFieldBottomSheetNavItem(prefix),
                                route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute()
                            )
                        }
                        is CustomFieldNavigation.CustomFieldOptions -> {
                            val prefix = CustomFieldPrefix.fromNote(backDestination)
                            appNavigator.navigate(
                                destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                                route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                                    index = cevent.index,
                                    currentTitle = cevent.currentValue
                                )
                            )
                        }
                        is CustomFieldNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                            val prefix = CustomFieldPrefix.fromNote(backDestination)
                            appNavigator.navigate(
                                destination = CustomFieldNameDialogNavItem(prefix),
                                route = CustomFieldNameDialogNavItem(prefix).buildRoute(
                                    type = cevent.type,
                                    sectionIndex = None
                                ),
                                backDestination = backDestination
                            )
                        }
                        is CustomFieldNavigation.EditCustomField -> dismissBottomSheet {
                            val prefix = CustomFieldPrefix.fromNote(backDestination)
                            appNavigator.navigate(
                                destination = EditCustomFieldNameDialogNavItem(prefix),
                                route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                                    index = cevent.index,
                                    sectionIndex = None,
                                    currentValue = cevent.currentValue
                                ),
                                backDestination = backDestination
                            )
                        }
                        CustomFieldNavigation.RemovedCustomField -> dismissBottomSheet {}
                    }
                BaseNoteNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                is BaseNoteNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromNote(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(index = it.index),
                        backDestination = backDestination
                    )
                }
                BaseNoteNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseNoteNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)

                is BaseNoteNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromNote(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(index = it.index)
                    )
                }

                is BaseNoteNavigation.OnCreateNoteEvent -> when (val cevent = it.event) {
                    CreateNoteNavigation.NoteCreated -> appNavigator.navigateBack()
                    is CreateNoteNavigation.SelectVault -> appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(cevent.shareId)
                    )
                }
                is BaseNoteNavigation.OnUpdateNoteEvent -> when (val cevent = it.event) {
                    is UpdateNoteNavigation.NoteUpdated -> appNavigator.navigate(
                        destination = ItemDetailsNavItem,
                        route = ItemDetailsNavItem.createNavRoute(cevent.shareId, cevent.itemId),
                        backDestination = HomeNavItem
                    )
                    is UpdateNoteNavigation.OpenAttachmentOptions -> appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(
                            shareId = cevent.shareId,
                            itemId = cevent.itemId,
                            attachmentId = cevent.attachmentId
                        )
                    )
                }
            }
        }
    )
    createUpdateCreditCardGraph(
        canUseAttachments = true,
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateCreditCardNavItem) -> CreateCreditCardNavItem
                appNavigator.hasDestinationInStack(EditCreditCardNavItem) -> EditCreditCardNavItem
                else -> null
            }
            when (it) {
                BaseCreditCardNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseCreditCardNavigation.DismissBottomsheet -> dismissBottomSheet {}
                is CreateCreditCardNavigation -> when (it) {
                    is CreateCreditCardNavigation.ItemCreated -> appNavigator.navigateBack()
                    is CreateCreditCardNavigation.SelectVault -> appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(it.shareId)
                    )
                }
                is UpdateCreditCardNavigation -> when (it) {
                    is UpdateCreditCardNavigation.ItemUpdated -> appNavigator.navigate(
                        destination = ItemDetailsNavItem,
                        route = ItemDetailsNavItem.createNavRoute(it.shareId, it.itemId),
                        backDestination = HomeNavItem
                    )
                }
                BaseCreditCardNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
                BaseCreditCardNavigation.AddAttachment -> appNavigator.navigate(AddAttachmentNavItem)
                is BaseCreditCardNavigation.OpenAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(
                            shareId = it.shareId,
                            itemId = it.itemId,
                            attachmentId = it.attachmentId
                        )
                    )

                is BaseCreditCardNavigation.OpenDraftAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                    )

                is BaseCreditCardNavigation.DeleteAllAttachments ->
                    appNavigator.navigate(
                        destination = DeleteAllAttachmentsDialogNavItem,
                        route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                    )

                BaseCreditCardNavigation.UpsellAttachments ->
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                    )

                BaseCreditCardNavigation.AddCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = AddCustomFieldBottomSheetNavItem(prefix),
                        route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute()
                    )
                }
                is BaseCreditCardNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                            index = it.index,
                            currentTitle = it.currentValue
                        )
                    )
                }
                is BaseCreditCardNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(it.type),
                        backDestination = backDestination
                    )
                }
                is BaseCreditCardNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentValue = it.currentValue
                        ),
                        backDestination = backDestination
                    )
                }

                is BaseCreditCardNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(index = it.index),
                        backDestination = backDestination
                    )
                }
                BaseCreditCardNavigation.RemovedCustomField -> dismissBottomSheet {}
                BaseCreditCardNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseCreditCardNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)

                is BaseCreditCardNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromCreditCard(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(index = it.index)
                    )
                }
            }
        }
    )
    createUpdateAliasGraph(
        canUseAttachments = true,
        canAddMailbox = true,
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateAliasNavItem) -> CreateAliasNavItem
                appNavigator.hasDestinationInStack(EditAliasNavItem) -> EditAliasNavItem
                else -> null
            }
            when (it) {
                is BaseAliasNavigation.OnCreateAliasEvent -> when (val event = it.event) {
                    is CreateAliasNavigation.Created -> appNavigator.navigateBack()
                    is CreateAliasNavigation.CreatedFromBottomsheet -> dismissBottomSheet {}
                    is CreateAliasNavigation.SelectVault -> appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(event.shareId)
                    )
                }
                is BaseAliasNavigation.OnUpdateAliasEvent -> when (val event = it.event) {
                    is UpdateAliasNavigation.OpenAttachmentOptions -> appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(
                            shareId = event.shareId,
                            itemId = event.itemId,
                            attachmentId = event.attachmentId
                        )
                    )
                    is UpdateAliasNavigation.Updated -> appNavigator.navigate(
                        destination = ItemDetailsNavItem,
                        route = ItemDetailsNavItem.createNavRoute(event.shareId, event.itemId),
                        backDestination = HomeNavItem
                    )
                }
                BaseAliasNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseAliasNavigation.CloseBottomsheet -> dismissBottomSheet {}
                BaseAliasNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)


                BaseAliasNavigation.AddAttachment ->
                    appNavigator.navigate(AddAttachmentNavItem)

                is BaseAliasNavigation.OpenDraftAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                    )

                is BaseAliasNavigation.DeleteAllAttachments ->
                    appNavigator.navigate(
                        destination = DeleteAllAttachmentsDialogNavItem,
                        route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                    )

                BaseAliasNavigation.UpsellAttachments ->
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                    )

                BaseAliasNavigation.SelectMailbox ->
                    appNavigator.navigate(AliasSelectMailboxBottomSheetNavItem)

                BaseAliasNavigation.SelectSuffix ->
                    appNavigator.navigate(AliasSelectSuffixBottomSheetNavItem)

                BaseAliasNavigation.AddMailbox -> dismissBottomSheet {
                    appNavigator.navigate(SimpleLoginSyncMailboxCreateNavItem)
                }

                BaseAliasNavigation.AddCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = AddCustomFieldBottomSheetNavItem(prefix),
                        route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute()
                    )
                }
                is BaseAliasNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                            index = it.index,
                            currentTitle = it.currentValue
                        )
                    )
                }
                is BaseAliasNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(it.type),
                        backDestination = backDestination
                    )
                }
                is BaseAliasNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentValue = it.currentValue
                        ),
                        backDestination = backDestination
                    )
                }
                is BaseAliasNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(index = it.index),
                        backDestination = backDestination
                    )
                }
                BaseAliasNavigation.RemovedCustomField -> dismissBottomSheet {}
                BaseAliasNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseAliasNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)

                is BaseAliasNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromAlias(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix).createNavRoute(index = it.index)
                    )
                }
            }
        }
    )
    createUpdateIdentityGraph(
        onNavigate = {
            val backDestination = when {
                appNavigator.hasDestinationInStack(CreateIdentityNavItem) -> CreateIdentityNavItem
                appNavigator.hasDestinationInStack(UpdateIdentityNavItem) -> UpdateIdentityNavItem
                else -> null
            }
            when (it) {
                BaseIdentityNavigation.CloseScreen -> appNavigator.navigateBack()
                BaseIdentityNavigation.DismissBottomsheet -> dismissBottomSheet { }
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
                    appNavigator.navigate(
                        destination = AddCustomFieldBottomSheetNavItem(prefix),
                        route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute(sectionIndex = None)
                    )
                }

                is BaseIdentityNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldNameDialogNavItem(prefix),
                        route = CustomFieldNameDialogNavItem(prefix).buildRoute(
                            type = it.type,
                            sectionIndex = None
                        ),
                        backDestination = backDestination
                    )
                }

                is BaseIdentityNavigation.EditCustomField -> dismissBottomSheet {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = EditCustomFieldNameDialogNavItem(prefix),
                        route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentValue = it.title
                        ),
                        backDestination = backDestination
                    )
                }

                is BaseIdentityNavigation.CustomFieldOptions -> {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                        route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                            index = it.index,
                            sectionIndex = None,
                            currentTitle = it.title
                        )
                    )
                }

                BaseIdentityNavigation.RemovedCustomField -> dismissBottomSheet {}

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

                BaseIdentityNavigation.RemoveCustomSection -> dismissBottomSheet {}

                is UpdateIdentityNavigation.IdentityUpdated -> appNavigator.navigate(
                    destination = ItemDetailsNavItem,
                    route = ItemDetailsNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    ),
                    backDestination = HomeNavItem
                )

                BaseIdentityNavigation.AddAttachment -> appNavigator.navigate(AddAttachmentNavItem)
                is BaseIdentityNavigation.OpenAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(
                            shareId = it.shareId,
                            itemId = it.itemId,
                            attachmentId = it.attachmentId
                        )
                    )

                is BaseIdentityNavigation.OpenDraftAttachmentOptions ->
                    appNavigator.navigate(
                        destination = AttachmentOptionsOnEditNavItem,
                        route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                    )

                is BaseIdentityNavigation.DeleteAllAttachments ->
                    appNavigator.navigate(
                        destination = DeleteAllAttachmentsDialogNavItem,
                        route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                    )

                BaseIdentityNavigation.UpsellAttachments ->
                    appNavigator.navigate(
                        destination = UpsellNavItem,
                        route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                    )

                is BaseIdentityNavigation.ScanTotp -> {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = CameraTotpNavItem(prefix),
                        route = CameraTotpNavItem(prefix)
                            .createNavRoute(
                                specialSectionIndex = it.section.toIndex().some(),
                                sectionIndex = (it.section as? IdentitySectionType.ExtraSection)
                                    ?.index
                                    .toOption(),
                                index = it.index.some()
                            )
                    )
                }
                is BaseIdentityNavigation.OpenImagePicker -> {
                    val prefix = CustomFieldPrefix.fromIdentity(backDestination)
                    appNavigator.navigate(
                        destination = PhotoPickerTotpNavItem(prefix),
                        route = PhotoPickerTotpNavItem(prefix).createNavRoute(
                            specialSectionIndex = it.specialIndex,
                            sectionIndex = it.sectionIndex,
                            index = it.index
                        ),
                        backDestination = backDestination
                    )
                }

                BaseIdentityNavigation.TotpCancel -> appNavigator.navigateBack()
                is BaseIdentityNavigation.TotpSuccess ->
                    appNavigator.navigateBackWithResult(it.results)
            }
        }
    )
    createUpdateCustomItemGraph {
        val backDestination = when {
            appNavigator.hasDestinationInStack(CreateCustomItemNavItem) -> CreateCustomItemNavItem
            appNavigator.hasDestinationInStack(UpdateCustomItemNavItem) -> UpdateCustomItemNavItem
            else -> null
        }
        when (it) {
            BaseCustomItemNavigation.CloseScreen -> appNavigator.navigateBack()
            BaseCustomItemNavigation.DismissBottomsheet -> dismissBottomSheet {}
            BaseCustomItemNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
            is CreateCustomItemNavigation.ItemCreated -> appNavigator.navigateBack()
            is CreateCustomItemNavigation.SelectVault -> appNavigator.navigate(
                destination = SelectVaultBottomsheet,
                route = SelectVaultBottomsheet.createNavRoute(it.shareId)
            )

            is BaseCustomItemNavigation.AddCustomField -> dismissBottomSheet {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = AddCustomFieldBottomSheetNavItem(prefix),
                    route = AddCustomFieldBottomSheetNavItem(prefix).buildRoute(
                        sectionIndex = it.sectionIndex
                    )
                )
            }

            is BaseCustomItemNavigation.CustomFieldOptions -> {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = CustomFieldOptionsBottomSheetNavItem(prefix),
                    route = CustomFieldOptionsBottomSheetNavItem(prefix).buildRoute(
                        index = it.index,
                        sectionIndex = it.sectionIndex,
                        currentTitle = it.title
                    )
                )
            }

            is BaseCustomItemNavigation.CustomFieldTypeSelected -> dismissBottomSheet {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = CustomFieldNameDialogNavItem(prefix),
                    route = CustomFieldNameDialogNavItem(prefix).buildRoute(
                        type = it.type,
                        sectionIndex = it.sectionIndex
                    ),
                    backDestination = backDestination
                )
            }

            is BaseCustomItemNavigation.EditCustomField -> dismissBottomSheet {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = EditCustomFieldNameDialogNavItem(prefix),
                    route = EditCustomFieldNameDialogNavItem(prefix).buildRoute(
                        index = it.index,
                        sectionIndex = it.sectionIndex,
                        currentValue = it.title
                    ),
                    backDestination = backDestination
                )
            }

            BaseCustomItemNavigation.AddAttachment ->
                appNavigator.navigate(AddAttachmentNavItem)

            is BaseCustomItemNavigation.OpenAttachmentOptions ->
                appNavigator.navigate(
                    destination = AttachmentOptionsOnEditNavItem,
                    route = AttachmentOptionsOnEditNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId,
                        attachmentId = it.attachmentId
                    )
                )

            is BaseCustomItemNavigation.OpenDraftAttachmentOptions ->
                appNavigator.navigate(
                    destination = AttachmentOptionsOnEditNavItem,
                    route = AttachmentOptionsOnEditNavItem.createNavRoute(it.uri)
                )

            BaseCustomItemNavigation.UpsellAttachments ->
                appNavigator.navigate(
                    destination = UpsellNavItem,
                    route = UpsellNavItem.createNavRoute(PaidFeature.FileAttachments)
                )

            is BaseCustomItemNavigation.DeleteAllAttachments ->
                appNavigator.navigate(
                    destination = DeleteAllAttachmentsDialogNavItem,
                    route = DeleteAllAttachmentsDialogNavItem.createNavRoute(it.attachmentIds)
                )

            BaseCustomItemNavigation.AddSection ->
                appNavigator.navigate(CustomSectionNameDialogNavItem)

            is BaseCustomItemNavigation.SectionOptions ->
                appNavigator.navigate(
                    destination = CustomSectionOptionsBottomSheetNavItem,
                    route = CustomSectionOptionsBottomSheetNavItem.buildRoute(it.index, it.title)
                )

            is BaseCustomItemNavigation.EditSection -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = EditCustomSectionNameDialogNavItem,
                    route = EditCustomSectionNameDialogNavItem.buildRoute(it.index, it.title),
                    backDestination = backDestination
                )
            }

            BaseCustomItemNavigation.RemoveCustomField -> dismissBottomSheet {}
            BaseCustomItemNavigation.RemoveSection -> dismissBottomSheet {}
            is UpdateCustomItemNavigation.ItemUpdated -> appNavigator.navigate(
                destination = ItemDetailsNavItem,
                route = ItemDetailsNavItem.createNavRoute(
                    shareId = it.shareId,
                    itemId = it.itemId
                ),
                backDestination = HomeNavItem
            )

            is BaseCustomItemNavigation.OpenTOTPScanner -> {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = CameraTotpNavItem(prefix),
                    route = CameraTotpNavItem(prefix).createNavRoute(
                        sectionIndex = it.sectionIndex,
                        index = it.index.some()
                    )
                )
            }

            is BaseCustomItemNavigation.OpenImagePicker -> {
                val prefix = CustomFieldPrefix.fromCustomItem(backDestination)
                appNavigator.navigate(
                    destination = PhotoPickerTotpNavItem(prefix),
                    route = PhotoPickerTotpNavItem(prefix).createNavRoute(
                        sectionIndex = it.sectionIndex,
                        index = it.index.some()
                    ),
                    backDestination = backDestination
                )
            }

            BaseCustomItemNavigation.TotpCancel -> appNavigator.navigateBack()
            is BaseCustomItemNavigation.TotpSuccess ->
                appNavigator.navigateBackWithResult(it.results)

            is BaseCustomItemNavigation.OpenWifiSecurityTypeSelector ->
                appNavigator.navigate(
                    destination = SelectWifiSecurityTypeNavItem,
                    route = SelectWifiSecurityTypeNavItem.createNavRoute(it.wifiSecurityType)
                )

            is BaseCustomItemNavigation.WifiSecurityTypeSelected -> {
                dismissBottomSheet {
                    appNavigator.setResult(
                        mapOf(
                            WIFI_SECURITY_TYPE_PARAMETER_KEY
                                to it.wifiSecurityType.id
                        )
                    )
                }
            }
        }
    }
    itemDetailsNavGraph(
        onNavigated = { itemDetailsNavDestination ->
            when (itemDetailsNavDestination) {
                ItemDetailsNavDestination.CloseScreen -> appNavigator.navigateBack()

                ItemDetailsNavDestination.Home -> dismissBottomSheet {
                    appNavigator.popUpTo(destination = HomeNavItem)
                }

                is ItemDetailsNavDestination.EditItem -> when (itemDetailsNavDestination.itemCategory) {
                    ItemCategory.Alias -> EditAliasNavItem to EditAliasNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )

                    ItemCategory.CreditCard -> EditCreditCardNavItem to EditCreditCardNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )

                    ItemCategory.Identity -> UpdateIdentityNavItem to UpdateIdentityNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )

                    ItemCategory.Login -> EditLoginNavItem to EditLoginNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )

                    ItemCategory.Note -> UpdateNoteNavItem to UpdateNoteNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )

                    ItemCategory.SSHKey,
                    ItemCategory.WifiNetwork,
                    ItemCategory.Custom ->
                        UpdateCustomItemNavItem to UpdateCustomItemNavItem.createNavRoute(
                            shareId = itemDetailsNavDestination.shareId,
                            itemId = itemDetailsNavDestination.itemId
                        )

                    ItemCategory.Password,
                    ItemCategory.Unknown -> throw IllegalStateException(
                        "Cannot edit items with category: ${itemDetailsNavDestination.itemCategory}"
                    )
                }.also { (editDestination, editRoute) ->
                    appNavigator.navigate(
                        destination = editDestination,
                        route = editRoute
                    )
                }

                is ItemDetailsNavDestination.DuplicateItem -> dismissBottomSheet {
                    val shareId = itemDetailsNavDestination.shareId.some()
                    val itemId = itemDetailsNavDestination.itemId.some()
                    when (itemDetailsNavDestination.category) {
                        ItemCategory.Login -> appNavigator.navigate(
                            destination = CreateLoginNavItem,
                            backDestination = HomeNavItem,
                            route = CreateLoginNavItem.createNavRoute(shareId, itemId)
                        )

                        ItemCategory.Note -> appNavigator.navigate(
                            destination = CreateNoteNavItem,
                            backDestination = HomeNavItem,
                            route = CreateNoteNavItem.createNavRoute(shareId, itemId)
                        )

                        ItemCategory.CreditCard -> appNavigator.navigate(
                            destination = CreateCreditCardNavItem,
                            backDestination = HomeNavItem,
                            route = CreateCreditCardNavItem.createNavRoute(shareId, itemId)
                        )

                        ItemCategory.Identity -> appNavigator.navigate(
                            destination = CreateIdentityNavItem,
                            backDestination = HomeNavItem,
                            route = CreateIdentityNavItem.createNavRoute(shareId, itemId)
                        )

                        ItemCategory.WifiNetwork, ItemCategory.SSHKey, ItemCategory.Custom -> appNavigator.navigate(
                            destination = CreateCustomItemNavItem,
                            backDestination = HomeNavItem,
                            route = CreateCustomItemNavItem.createNavRoute(shareId, itemId)
                        )

                        ItemCategory.Alias, ItemCategory.Password, ItemCategory.Unknown -> Unit // No-opt
                    }

                }

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
                    backDestination = ItemDetailsNavItem
                )

                is ItemDetailsNavDestination.ItemOptionsMenu -> appNavigator.navigate(
                    destination = ItemDetailsMenuNavItem,
                    route = ItemDetailsMenuNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.ItemTrashMenu -> appNavigator.navigate(
                    destination = ItemTrashMenuNavItem,
                    route = ItemTrashMenuNavItem.createNavRoute(
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

                ItemDetailsNavDestination.DismissBottomSheet -> dismissBottomSheet {}

                ItemDetailsNavDestination.ItemSharedMigration -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateSharedWarningNavItem,
                        route = MigrateSharedWarningNavItem.createNavRoute(
                            migrateMode = MigrateModeValue.SelectedItems,
                            filter = MigrateVaultFilter.All
                        )
                    )
                }

                is ItemDetailsNavDestination.ItemActionForbidden -> appNavigator.navigate(
                    destination = ItemDetailsForbiddenNavItem,
                    route = ItemDetailsForbiddenNavItem.createNavRoute(
                        reason = itemDetailsNavDestination.reason
                    )
                )

                ItemDetailsNavDestination.Upgrade -> onNavigate(AppNavigation.Upgrade)

                is ItemDetailsNavDestination.LeaveItemShare -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = ItemDetailsLeaveNavItem,
                        route = ItemDetailsLeaveNavItem.createNavRoute(
                            shareId = itemDetailsNavDestination.shareId
                        )
                    )
                }

                is ItemDetailsNavDestination.WifiNetworkQRClick -> appNavigator.navigate(
                    destination = QRViewerNavItem,
                    route = QRViewerNavItem.createNavRoute(itemDetailsNavDestination.rawSVG)
                )

                is ItemDetailsNavDestination.OpenAttachmentOptions -> appNavigator.navigate(
                    destination = AttachmentOptionsOnDetailNavItem,
                    route = AttachmentOptionsOnDetailNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId,
                        attachmentId = itemDetailsNavDestination.attachmentId
                    )
                )

                is ItemDetailsNavDestination.ViewReusedPasswords -> appNavigator.navigate(
                    destination = LoginItemDetailsReusedPassNavItem,
                    route = LoginItemDetailsReusedPassNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.ContactSection -> appNavigator.navigate(
                    destination = DetailAliasContactNavItem,
                    route = DetailAliasContactNavItem.createNavRoute(
                        shareId = itemDetailsNavDestination.shareId,
                        itemId = itemDetailsNavDestination.itemId
                    )
                )

                is ItemDetailsNavDestination.OnCreateLoginFromAlias -> appNavigator.navigate(
                    destination = CreateLoginNavItem,
                    route = CreateLoginNavItem.createNavRoute(
                        emailOption = itemDetailsNavDestination.alias.some(),
                        shareId = itemDetailsNavDestination.shareId.some()
                    ),
                    backDestination = HomeNavItem
                )
            }
        }
    )

    itemHistoryNavGraph(
        onNavigated = { itemHistoryNavDestination ->
            when (itemHistoryNavDestination) {
                ItemHistoryNavDestination.Upgrade -> onNavigate(AppNavigation.Upgrade)
                ItemHistoryNavDestination.CloseScreen -> appNavigator.navigateBack()

                is ItemHistoryNavDestination.Detail -> appNavigator.popUpTo(ItemDetailsNavItem)

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

                is ItemHistoryNavDestination.ConfirmResetHistory -> dismissBottomSheet {
                    appNavigator.navigateBack(true)
                    appNavigator.navigate(
                        destination = ConfirmResetHistoryDialogNavItem,
                        route = ConfirmResetHistoryDialogNavItem.createNavRoute(
                            shareId = itemHistoryNavDestination.shareId,
                            itemId = itemHistoryNavDestination.itemId
                        )
                    )
                }

                is ItemHistoryNavDestination.Options -> appNavigator.navigate(
                    destination = HistoryOptionsNavItem,
                    route = HistoryOptionsNavItem.createNavRoute(
                        shareId = itemHistoryNavDestination.shareId,
                        itemId = itemHistoryNavDestination.itemId
                    )
                )

                is ItemHistoryNavDestination.WifiNetworkQR -> appNavigator.navigate(
                    destination = QRViewerNavItem,
                    route = QRViewerNavItem.createNavRoute(itemHistoryNavDestination.rawSVG)
                )
            }
        }
    )

    itemTrashNavGraph(
        onNavigated = { itemTrashNavDestination ->
            when (itemTrashNavDestination) {
                ItemTrashNavDestination.CloseScreen -> appNavigator.navigateBack()

                ItemTrashNavDestination.Home -> dismissBottomSheet {
                    appNavigator.popUpTo(destination = HomeNavItem)
                }

                ItemTrashNavDestination.DismissBottomSheet -> dismissBottomSheet {}

                is ItemTrashNavDestination.DeleteItem -> appNavigator.navigate(
                    destination = ItemTrashDeleteNavItem,
                    route = ItemTrashDeleteNavItem.createNavRoute(
                        shareId = itemTrashNavDestination.shareId,
                        itemId = itemTrashNavDestination.itemId
                    )
                )

                is ItemTrashNavDestination.LeaveItem -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = ItemDetailsLeaveNavItem,
                        route = ItemDetailsLeaveNavItem.createNavRoute(
                            shareId = itemTrashNavDestination.shareId
                        )
                    )
                }
            }
        }
    )

    migrateGraph(
        navigation = {
            when (it) {
                MigrateNavigation.Close -> appNavigator.navigateBack()

                is MigrateNavigation.VaultSelectedForMigrateItem -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateConfirmVault,
                        route = MigrateConfirmVault.createNavRouteForMigrateSelectedItems(
                            destShareId = it.destShareId
                        ),
                        backDestination = ItemDetailsNavItem
                    )
                }

                is MigrateNavigation.ItemMigrated -> dismissBottomSheet {
                    // Only navigate to detail if we already were in a detail screen
                    if (appNavigator.hasDestinationInStack(ItemDetailsNavItem)) {
                        appNavigator.navigate(
                            destination = ItemDetailsNavItem,
                            route = ItemDetailsNavItem.createNavRoute(it.shareId, it.itemId),
                            backDestination = HomeNavItem
                        )
                    } else if (appNavigator.hasDestinationInStack(HomeNavItem)) {
                        appNavigator.popUpTo(HomeNavItem)
                    }
                }


                MigrateNavigation.VaultMigrated -> dismissBottomSheet {}

                is MigrateNavigation.VaultSelectedForMigrateAll -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = MigrateConfirmVault,
                        route = MigrateConfirmVault.createNavRouteForMigrateAll(
                            sourceShareId = it.sourceShareId,
                            destShareId = it.destShareId
                        ),
                        backDestination = HomeNavItem
                    )
                }

                MigrateNavigation.DismissBottomsheet -> dismissBottomSheet {}

                is MigrateNavigation.VaultSelectionForItemsMigration -> {
                    appNavigator.navigate(
                        destination = MigrateSelectVault,
                        route = MigrateSelectVault.createNavRouteForMigrateSelectedItems(
                            filter = it.filter
                        ),
                        backDestination = MigrateSharedWarningNavItem
                    )
                }

                is MigrateNavigation.VaultSelectionForVaultMigration -> {
                    appNavigator.navigate(
                        destination = MigrateSelectVault,
                        route = MigrateSelectVault.createNavRouteForMigrateAll(
                            shareId = it.shareId
                        ),
                        backDestination = MigrateSharedWarningNavItem
                    )
                }
            }
        }
    )
    authGraph(
        canLogout = true,
        navigation = {
            when (it) {
                is AuthNavigation.CloseScreen -> when (it.origin) {
                    AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY,
                    AuthOrigin.EXTRA_PASSWORD_REMOVE,
                    AuthOrigin.EXTRA_PASSWORD_CONFIGURE -> appNavigator.navigateBack()

                    AuthOrigin.AUTO_LOCK,
                    AuthOrigin.EXTRA_PASSWORD_LOGIN -> onNavigate(AppNavigation.Finish)
                }

                is AuthNavigation.Success -> when (it.origin) {
                    AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY ->
                        dismissBottomSheet {
                            appNavigator.setResult(mapOf(ENTER_PIN_PARAMETER_KEY to true))
                        }

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

                AuthNavigation.Dismissed -> onNavigate(AppNavigation.Finish)
                AuthNavigation.Failed -> appNavigator.navigateBack()
                is AuthNavigation.SignOut -> onNavigate(AppNavigation.SignOut(it.userId))
                is AuthNavigation.ForceSignOut -> onNavigate(AppNavigation.ForceSignOut(it.userId))
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    destination = EnterPin,
                    route = EnterPin.buildRoute(it.origin)
                )

                AuthNavigation.ForceSignOutAllUsers -> onNavigate(AppNavigation.ForceSignOutAllUsers)
                AuthNavigation.CloseBottomsheet -> dismissBottomSheet {}
            }
        }
    )
    onBoardingGraph(
        onOnBoardingFinished = { appNavigator.popUpTo(HomeNavItem) },
        onNavigateBack = { onNavigate(AppNavigation.Finish) }
    )
    featureFlagsGraph()
    trialGraph {
        when (it) {
            TrialNavigation.CloseScreen -> appNavigator.navigateBack()
            TrialNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }

    securityCenterNavGraph(
        onNavigated = { destination ->
            when (destination) {
                is SecurityCenterNavDestination.Back ->
                    if (destination.comesFromBottomSheet) {
                        dismissBottomSheet {}
                    } else {
                        appNavigator.navigateBack(force = destination.force)
                    }

                SecurityCenterNavDestination.Home -> appNavigator.navigate(
                    destination = SecurityCenterHomeNavItem
                )

                SecurityCenterNavDestination.MainHome -> appNavigator.navigate(
                    destination = HomeNavItem
                )

                is SecurityCenterNavDestination.ItemDetails -> appNavigator.navigate(
                    destination = ItemDetailsNavItem,
                    route = ItemDetailsNavItem.createNavRoute(
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
                    destination = CreateItemBottomsheetNavItem,
                    route = CreateItemBottomsheetNavItem.createNavRoute(CreateItemBottomSheetMode.HomeFull)
                )

                SecurityCenterNavDestination.MainProfile -> appNavigator.navigate(
                    destination = ProfileNavItem
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
                        email = destination.email
                    )
                )

                is SecurityCenterNavDestination.AliasEmailReport -> appNavigator.navigate(
                    destination = SecurityCenterAliasEmailReportNavItem,
                    route = SecurityCenterAliasEmailReportNavItem.createNavRoute(
                        id = destination.id,
                        email = destination.email
                    )
                )

                is SecurityCenterNavDestination.ProtonEmailReport -> appNavigator.navigate(
                    destination = SecurityCenterProtonEmailReportNavItem,
                    route = SecurityCenterProtonEmailReportNavItem.createNavRoute(
                        id = destination.id,
                        email = destination.email
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
            SharingNavigation.CloseScreen -> appNavigator.navigateBack()

            SharingNavigation.BackToHome -> dismissBottomSheet {
                appNavigator.popUpTo(HomeNavItem)
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
                    appNavigator.setResult(
                        mapOf(REFRESH_MEMBER_LIST_FLAG to true)
                    )
                }
            }

            is SharingNavigation.CloseDialog -> {
                appNavigator.navigateBack()

                if (it.refresh) {
                    appNavigator.setResult(
                        mapOf(REFRESH_MEMBER_LIST_FLAG to true)
                    )
                }
            }

            is SharingNavigation.Permissions -> appNavigator.navigate(
                destination = SharingPermissions,
                route = SharingPermissions.createRoute(
                    shareId = it.shareId,
                    itemIdOption = it.itemIdOption
                )
            )

            is SharingNavigation.Summary -> appNavigator.navigate(
                destination = SharingSummary,
                route = SharingSummary.createRoute(
                    shareId = it.shareId,
                    itemIdOption = it.itemIdOption
                )
            )

            is SharingNavigation.ShareVault -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = SharingWith,
                    route = SharingWith.createRoute(
                        shareId = it.shareId,
                        showEditVault = false,
                        itemIdOption = None
                    )
                )
            }

            is SharingNavigation.ManageItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = ManageItemNavItem,
                    route = ManageItemNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId
                    )
                )
            }

            is SharingNavigation.ManageItemMemberOptions -> appNavigator.navigate(
                destination = ManageItemMemberOptionsNavItem,
                route = ManageItemMemberOptionsNavItem.createNavRoute(
                    shareId = it.shareId,
                    memberShareId = it.memberShareId,
                    memberShareRole = it.memberRole,
                    memberEmail = it.memberEmail
                )
            )

            is SharingNavigation.ManageItemInviteOptions -> appNavigator.navigate(
                destination = ManageItemInviteOptionsNavItem,
                route = ManageItemInviteOptionsNavItem.createNavRoute(
                    shareId = it.shareId,
                    inviteId = it.pendingInvite.inviteId,
                    isForNewUser = it.pendingInvite.isForNewUser
                )
            )

            is SharingNavigation.ManageVault -> appNavigator.navigate(
                destination = ManageVault,
                route = ManageVault.createRoute(it.shareId),
                backDestination = HomeNavItem
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

            is SharingNavigation.InviteToShareEditPermissions -> appNavigator.navigate(
                destination = SharingEditPermissions,
                route = SharingEditPermissions.buildRouteForEditOne(
                    itemIdOption = it.itemIdOption,
                    email = it.email,
                    permission = it.permission.toSharingType()
                )
            )

            is SharingNavigation.InviteToShareEditAllPermissions -> appNavigator.navigate(
                destination = SharingEditPermissions,
                route = SharingEditPermissions.buildRouteForEditAll(
                    itemIdOption = it.itemIdOption
                )
            )

            SharingNavigation.InviteError -> appNavigator.navigate(
                destination = InvitesErrorDialog
            )

            is SharingNavigation.ShareItemLink -> dismissBottomSheet {
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

            is SharingNavigation.ShareItem -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = SharingWith,
                    route = SharingWith.createRoute(
                        shareId = it.shareId,
                        showEditVault = false,
                        itemIdOption = it.itemId.some()
                    )
                )
            }

            is SharingNavigation.ItemDetails -> appNavigator.popUpTo(ItemDetailsNavItem)
            is SharingNavigation.SharedItemDetails -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = ItemDetailsNavItem,
                    route = ItemDetailsNavItem.createNavRoute(it.shareId, it.itemId),
                    backDestination = HomeNavItem
                )
            }

            SharingNavigation.ShareItemNewUsersError -> appNavigator.popUpTo(
                destination = SharingWith
            )
        }
    }

    syncNavGraph { destination ->
        when (destination) {
            SyncNavDestination.CloseScreen -> appNavigator.navigateBack()
        }
    }

    upsellNavGraph(
        onNavigated = { upsellNavDestination ->
            when (upsellNavDestination) {
                UpsellNavDestination.CloseScreen -> appNavigator.navigateBack()
                UpsellNavDestination.Upgrade -> onNavigate(AppNavigation.Upgrade)
                UpsellNavDestination.Subscription -> onNavigate(AppNavigation.Subscription)
            }
        }
    )

    secureLinksNavGraph(
        onNavigated = { destination ->
            when (destination) {
                SecureLinksNavDestination.CloseScreen -> appNavigator.navigateBack()

                is SecureLinksNavDestination.CloseScreenWithCategory ->
                    appNavigator.popUpTo(ItemDetailsNavItem)

                SecureLinksNavDestination.DismissBottomSheet -> dismissBottomSheet {}

                SecureLinksNavDestination.Profile ->
                    if (appNavigator.hasDestinationInStack(ProfileNavItem)) {
                        appNavigator.popUpTo(ProfileNavItem)
                    } else {
                        appNavigator.popUpTo(HomeNavItem)
                    }

                is SecureLinksNavDestination.SecureLinkOverview -> when (destination.scope) {
                    SecureLinksOverviewNavScope.SecureLinksGeneration -> appNavigator.navigate(
                        destination = SecureLinksOverviewScreenNavItem,
                        route = SecureLinksOverviewScreenNavItem.createNavRoute(destination.secureLinkId),
                        backDestination = ItemDetailsNavItem
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

    simpleLoginSyncNavGraph(
        onNavigated = { destination ->
            when (destination) {
                SimpleLoginSyncNavDestination.BackToOrigin -> when {
                    appNavigator.hasDestinationInStack(SimpleLoginSyncManagementNavItem) ->
                        appNavigator.popUpTo(SimpleLoginSyncManagementNavItem)

                    appNavigator.hasDestinationInStack(CreateAliasNavItem) ->
                        appNavigator.popUpTo(CreateAliasNavItem)

                    appNavigator.hasDestinationInStack(EditAliasNavItem) ->
                        appNavigator.popUpTo(EditAliasNavItem)
                }

                is SimpleLoginSyncNavDestination.CloseScreen -> appNavigator.navigateBack(
                    force = destination.force
                )

                SimpleLoginSyncNavDestination.CreateMailbox -> appNavigator.navigate(
                    destination = SimpleLoginSyncMailboxCreateNavItem
                )

                is SimpleLoginSyncNavDestination.SelectVault -> appNavigator.navigate(
                    destination = SelectVaultBottomsheet,
                    route = SelectVaultBottomsheet.createNavRoute(
                        selectedVault = destination.shareId
                    )
                )

                is SimpleLoginSyncNavDestination.Settings -> appNavigator.navigate(
                    destination = SimpleLoginSyncSettingsNavItem,
                    route = SimpleLoginSyncSettingsNavItem.createNavRoute(destination.shareId)
                )

                SimpleLoginSyncNavDestination.Upsell -> appNavigator.navigate(
                    destination = UpsellNavItem,
                    route = UpsellNavItem.createNavRoute(
                        paidFeature = PaidFeature.AdvanceAliasManagement
                    )
                )

                is SimpleLoginSyncNavDestination.VerifyMailbox -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SimpleLoginSyncMailboxVerifyNavItem,
                        route = SimpleLoginSyncMailboxVerifyNavItem.buildRoute(
                            mailboxId = destination.mailboxId
                        ),
                        backDestination = SimpleLoginSyncManagementNavItem
                    )
                }

                is SimpleLoginSyncNavDestination.SelectDomain -> appNavigator.navigate(
                    destination = SimpleLoginSyncDomainSelectNavItem,
                    route = SimpleLoginSyncDomainSelectNavItem.buildRoute(
                        canSelectPremiumDomains = destination.canSelectPremiumDomains
                    )
                )

                is SimpleLoginSyncNavDestination.MailboxOptions -> appNavigator.navigate(
                    destination = SimpleLoginSyncMailboxOptionsNavItem,
                    route = SimpleLoginSyncMailboxOptionsNavItem.createNavRoute(
                        mailboxId = destination.mailboxId
                    )
                )

                is SimpleLoginSyncNavDestination.DeleteMailbox -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SimpleLoginSyncMailboxDeleteNavItem,
                        route = SimpleLoginSyncMailboxDeleteNavItem.createNavRoute(
                            mailboxId = destination.mailboxId
                        ),
                        backDestination = SimpleLoginSyncManagementNavItem
                    )
                }

                SimpleLoginSyncNavDestination.DismissBottomSheet -> dismissBottomSheet {}

                is SimpleLoginSyncNavDestination.ChangeMailboxEmail -> dismissBottomSheet {
                    appNavigator.navigate(
                        destination = SimpleLoginSyncMailboxChangeNavItem,
                        route = SimpleLoginSyncMailboxChangeNavItem.buildRoute(
                            mailboxId = destination.mailboxId
                        ),
                        backDestination = SimpleLoginSyncManagementNavItem
                    )
                }
            }
        }
    )

    reportNavGraph(
        onNavigated = { destination ->
            when (destination) {
                ReportNavDestination.CloseScreen -> appNavigator.navigateBack()
            }
        }
    )

    itemOptionsNavGraph(
        onNavigated = { destination ->
            when (destination) {
                ItemOptionsNavDestination.CloseScreen -> appNavigator.navigateBack()

                ItemOptionsNavDestination.TrashItem -> {
                    appNavigator.navigateBackWithResult(
                        key = CommonNavArgKey.ITEM_MOVED_TO_TRASH,
                        value = true
                    )
                }
            }
        }
    )

    aliasContactGraph {
        when (it) {
            AliasContactsNavigation.CloseScreen -> appNavigator.navigateBack()
            is AliasContactsNavigation.CreateContact -> appNavigator.navigate(
                destination = CreateAliasContactNavItem,
                route = CreateAliasContactNavItem.createNavRoute(it.shareId, it.itemId)
            )

            is AliasContactsNavigation.OnBoardingContacts -> appNavigator.navigate(
                destination = OnBoardingAliasContactNavItem
            )

            is AliasContactsNavigation.ContactOptions -> appNavigator.navigate(
                destination = OptionsAliasContactNavItem,
                route = OptionsAliasContactNavItem.createNavRoute(
                    shareId = it.shareId,
                    itemId = it.itemId,
                    contactId = it.contactId
                )
            )

            AliasContactsNavigation.CloseBottomSheet -> dismissBottomSheet {}

            AliasContactsNavigation.Upgrade -> onNavigate(AppNavigation.Upgrade)
        }
    }

    inAppMessageGraph {
        when (it) {
            InAppMessageModalDestination.CloseBottomsheet -> dismissBottomSheet {}

            is InAppMessageModalDestination.DeepLink -> dismissBottomSheet {
                if (it.deepLink.isNotBlank()) {
                    appNavigator.navigateToDeeplink(deepLink = it.deepLink)
                }
            }
        }
    }

    accountRedirectsGraph {
        when (it) {
            AccountRedirectsDestination.Upgrade -> {
                onNavigate(AppNavigation.Upgrade)
                appNavigator.navigateBackToStartDestination(force = true)
            }
        }
    }

    attachmentsGraph {
        when (it) {
            AttachmentsNavigation.CloseBottomsheet -> dismissBottomSheet {}

            AttachmentsNavigation.CloseScreen -> appNavigator.navigateBack(force = true)
            AttachmentsNavigation.OpenFilePicker -> dismissBottomSheet {
                appNavigator.navigate(FilePickerNavItem)
            }

            AttachmentsNavigation.OpenMediaPicker -> dismissBottomSheet {
                appNavigator.navigate(MediaPickerNavItem)
            }

            AttachmentsNavigation.OpenCamera -> dismissBottomSheet {
                appNavigator.navigate(CameraNavItem)
            }

            is AttachmentsNavigation.OpenRenameAttachment -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = RenameAttachmentNavItem,
                    route = RenameAttachmentNavItem.createNavRoute(
                        shareId = it.shareId,
                        itemId = it.itemId,
                        attachmentId = it.attachmentId
                    )
                )
            }

            is AttachmentsNavigation.OpenRenameDraftAttachment -> dismissBottomSheet {
                appNavigator.navigate(
                    destination = RenameAttachmentNavItem,
                    route = RenameAttachmentNavItem.createNavRoute(it.uri)
                )
            }

            AttachmentsNavigation.Upgrade -> dismissBottomSheet {
                onNavigate(AppNavigation.Upgrade)
            }
        }
    }
}
