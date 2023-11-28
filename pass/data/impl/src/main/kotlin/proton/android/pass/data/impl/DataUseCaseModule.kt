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

package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CancelInvite
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.ConfirmNewUserInvite
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.android.pass.data.api.usecases.CreatePin
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetAddressById
import proton.android.pass.data.api.usecases.GetAddressesForUserId
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.api.usecases.GetDefaultBrowser
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.GetPublicSuffixList
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedCreditCardItems
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.LeaveVault
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveAccounts
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveHasConfirmedInvite
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.data.api.usecases.RemoveMemberFromVault
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.data.api.usecases.ResendInvite
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.SetVaultMemberPermission
import proton.android.pass.data.api.usecases.TransferVaultOwnership
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.data.api.usecases.UserPlanWorkerLauncher
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionItemFiltererImpl
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.data.impl.autofill.SuggestionSorterImpl
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.data.impl.usecases.AcceptInviteImpl
import proton.android.pass.data.impl.usecases.ApplyPendingEventsImpl
import proton.android.pass.data.impl.usecases.CanDisplayTotpImpl
import proton.android.pass.data.impl.usecases.CanPerformPaidActionImpl
import proton.android.pass.data.impl.usecases.CancelInviteImpl
import proton.android.pass.data.impl.usecases.CheckMasterPasswordImpl
import proton.android.pass.data.impl.usecases.CheckPinImpl
import proton.android.pass.data.impl.usecases.ClearPinImpl
import proton.android.pass.data.impl.usecases.ClearTrashImpl
import proton.android.pass.data.impl.usecases.ClearUserDataImpl
import proton.android.pass.data.impl.usecases.ConfirmNewUserInviteImpl
import proton.android.pass.data.impl.usecases.CreateAliasImpl
import proton.android.pass.data.impl.usecases.CreateItemAndAliasImpl
import proton.android.pass.data.impl.usecases.CreateItemImpl
import proton.android.pass.data.impl.usecases.CreatePinImpl
import proton.android.pass.data.impl.usecases.CreateVaultImpl
import proton.android.pass.data.impl.usecases.DeleteItemImpl
import proton.android.pass.data.impl.usecases.DeleteVaultImpl
import proton.android.pass.data.impl.usecases.GetAddressByIdImpl
import proton.android.pass.data.impl.usecases.GetAddressesForUserIdImpl
import proton.android.pass.data.impl.usecases.GetAliasDetailsImpl
import proton.android.pass.data.impl.usecases.GetAllKeysByAddressImpl
import proton.android.pass.data.impl.usecases.GetDefaultBrowserImpl
import proton.android.pass.data.impl.usecases.GetInviteUserModeImpl
import proton.android.pass.data.impl.usecases.GetItemActionsImpl
import proton.android.pass.data.impl.usecases.GetItemByAliasEmailImpl
import proton.android.pass.data.impl.usecases.GetItemByIdImpl
import proton.android.pass.data.impl.usecases.GetItemByIdWithVaultImpl
import proton.android.pass.data.impl.usecases.GetPublicSuffixListImpl
import proton.android.pass.data.impl.usecases.GetShareByIdImpl
import proton.android.pass.data.impl.usecases.GetSuggestedCreditCardItemsImpl
import proton.android.pass.data.impl.usecases.GetSuggestedLoginItemsImpl
import proton.android.pass.data.impl.usecases.GetUserPlanImpl
import proton.android.pass.data.impl.usecases.GetVaultByIdImpl
import proton.android.pass.data.impl.usecases.GetVaultMembersImpl
import proton.android.pass.data.impl.usecases.GetVaultWithItemCountByIdImpl
import proton.android.pass.data.impl.usecases.InviteToVaultImpl
import proton.android.pass.data.impl.usecases.LeaveVaultImpl
import proton.android.pass.data.impl.usecases.MigrateItemImpl
import proton.android.pass.data.impl.usecases.MigrateVaultImpl
import proton.android.pass.data.impl.usecases.ObserveAccountsImpl
import proton.android.pass.data.impl.usecases.ObserveActiveItemsImpl
import proton.android.pass.data.impl.usecases.ObserveAliasOptionsImpl
import proton.android.pass.data.impl.usecases.ObserveAllSharesImpl
import proton.android.pass.data.impl.usecases.ObserveCurrentUserImpl
import proton.android.pass.data.impl.usecases.ObserveDefaultVaultImpl
import proton.android.pass.data.impl.usecases.ObserveHasConfirmedInviteImpl
import proton.android.pass.data.impl.usecases.ObserveInvitesImpl
import proton.android.pass.data.impl.usecases.ObserveItemCountImpl
import proton.android.pass.data.impl.usecases.ObserveItemsImpl
import proton.android.pass.data.impl.usecases.ObserveMFACountImpl
import proton.android.pass.data.impl.usecases.ObservePrimaryUserEmailImpl
import proton.android.pass.data.impl.usecases.ObserveUpgradeInfoImpl
import proton.android.pass.data.impl.usecases.ObserveUserAccessDataImpl
import proton.android.pass.data.impl.usecases.ObserveVaultByIdImpl
import proton.android.pass.data.impl.usecases.ObserveVaultCountImpl
import proton.android.pass.data.impl.usecases.ObserveVaultsImpl
import proton.android.pass.data.impl.usecases.ObserveVaultsWithItemCountImpl
import proton.android.pass.data.impl.usecases.PerformSyncImpl
import proton.android.pass.data.impl.usecases.RefreshContentImpl
import proton.android.pass.data.impl.usecases.RefreshInvitesImpl
import proton.android.pass.data.impl.usecases.RefreshPlanImpl
import proton.android.pass.data.impl.usecases.RejectInviteImpl
import proton.android.pass.data.impl.usecases.RemoveMemberFromVaultImpl
import proton.android.pass.data.impl.usecases.RequestImageImpl
import proton.android.pass.data.impl.usecases.ResendInviteImpl
import proton.android.pass.data.impl.usecases.ResetAppToDefaultsImpl
import proton.android.pass.data.impl.usecases.RestoreItemImpl
import proton.android.pass.data.impl.usecases.RestoreItemsImpl
import proton.android.pass.data.impl.usecases.SendUserAccessRequest
import proton.android.pass.data.impl.usecases.SendUserAccessRequestImpl
import proton.android.pass.data.impl.usecases.SetVaultMemberPermissionImpl
import proton.android.pass.data.impl.usecases.TransferVaultOwnershipImpl
import proton.android.pass.data.impl.usecases.TrashItemImpl
import proton.android.pass.data.impl.usecases.UpdateAliasImpl
import proton.android.pass.data.impl.usecases.UpdateAutofillItemImpl
import proton.android.pass.data.impl.usecases.UpdateItemImpl
import proton.android.pass.data.impl.usecases.UpdateVaultImpl
import proton.android.pass.data.impl.usecases.UserPlanWorkerLauncherImpl
import proton.android.pass.data.impl.usecases.capabilities.CanCreateItemInVaultImpl
import proton.android.pass.data.impl.usecases.capabilities.CanCreateVaultImpl
import proton.android.pass.data.impl.usecases.capabilities.CanManageVaultAccessImpl
import proton.android.pass.data.impl.usecases.capabilities.CanMigrateVaultImpl
import proton.android.pass.data.impl.usecases.capabilities.CanShareVaultImpl
import proton.android.pass.data.impl.usecases.defaultvault.SetDefaultVaultImpl
import proton.android.pass.data.impl.usecases.searchentry.AddSearchEntryImpl
import proton.android.pass.data.impl.usecases.searchentry.DeleteAllSearchEntryImpl
import proton.android.pass.data.impl.usecases.searchentry.DeleteSearchEntryImpl
import proton.android.pass.data.impl.usecases.searchentry.ObserveSearchEntryImpl

@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
abstract class DataUseCaseModule {

    @Binds
    abstract fun bindCreateAlias(impl: CreateAliasImpl): CreateAlias

    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindUpdateItem(impl: UpdateItemImpl): UpdateItem

    @Binds
    abstract fun bindCreateVault(impl: CreateVaultImpl): CreateVault

    @Binds
    abstract fun bindDeleteVault(impl: DeleteVaultImpl): DeleteVault

    @Binds
    abstract fun bindMigrateVault(impl: MigrateVaultImpl): MigrateVault

    @Binds
    abstract fun bindGetAddressById(impl: GetAddressByIdImpl): GetAddressById

    @Binds
    abstract fun bindGetAddressesForUserId(impl: GetAddressesForUserIdImpl): GetAddressesForUserId

    @Binds
    abstract fun bindObserveAliasOptions(impl: ObserveAliasOptionsImpl): ObserveAliasOptions

    @Binds
    abstract fun bindGetShareById(impl: GetShareByIdImpl): GetShareById

    @Binds
    abstract fun bindGetItemById(impl: GetItemByIdImpl): GetItemById

    @Binds
    abstract fun bindGetAliasDetails(impl: GetAliasDetailsImpl): GetAliasDetails

    @Binds
    abstract fun bindGetSuggestedLoginItems(impl: GetSuggestedLoginItemsImpl): GetSuggestedLoginItems

    @Binds
    abstract fun bindGetSuggestedCreditCardItems(impl: GetSuggestedCreditCardItemsImpl): GetSuggestedCreditCardItems

    @Binds
    abstract fun bindObserveAccounts(impl: ObserveAccountsImpl): ObserveAccounts

    @Binds
    abstract fun bindObserveActiveItems(impl: ObserveActiveItemsImpl): ObserveActiveItems

    @Binds
    abstract fun bindObserveCurrentUser(impl: ObserveCurrentUserImpl): ObserveCurrentUser

    @Binds
    abstract fun bindObserveItems(impl: ObserveItemsImpl): ObserveItems

    @Binds
    abstract fun bindObserveShares(impl: ObserveAllSharesImpl): ObserveAllShares

    @Binds
    abstract fun bindObserveVaults(impl: ObserveVaultsImpl): ObserveVaults

    @Binds
    abstract fun bindRefreshContent(impl: RefreshContentImpl): RefreshContent

    @Binds
    abstract fun bindTrashItem(impl: TrashItemImpl): TrashItem

    @Binds
    abstract fun bindUpdateAlias(impl: UpdateAliasImpl): UpdateAlias

    @Binds
    abstract fun bindUpdateAutofillItem(impl: UpdateAutofillItemImpl): UpdateAutofillItem

    @Binds
    abstract fun bindApplyPendingEvents(impl: ApplyPendingEventsImpl): ApplyPendingEvents

    @Binds
    abstract fun bindGetPublicSuffixList(impl: GetPublicSuffixListImpl): GetPublicSuffixList

    @Binds
    abstract fun bindSuggestionItemFilterer(impl: SuggestionItemFiltererImpl): SuggestionItemFilterer

    @Binds
    abstract fun bindHostParser(impl: HostParserImpl): HostParser

    @Binds
    abstract fun bindSuggestionSorter(impl: SuggestionSorterImpl): SuggestionSorter

    @Binds
    abstract fun bindRequestImage(impl: RequestImageImpl): RequestImage

    @Binds
    abstract fun bindObserveVaultsWithItemCount(
        impl: ObserveVaultsWithItemCountImpl
    ): ObserveVaultsWithItemCount

    @Binds
    abstract fun bindObserveItemCount(impl: ObserveItemCountImpl): ObserveItemCount

    @Binds
    abstract fun bindUpdateVault(impl: UpdateVaultImpl): UpdateVault

    @Binds
    abstract fun bindGetVaultById(impl: GetVaultByIdImpl): GetVaultById

    @Binds
    abstract fun bindSendUserAccess(impl: UserPlanWorkerLauncherImpl): UserPlanWorkerLauncher

    @Binds
    abstract fun bindSendUserAccessRequest(impl: SendUserAccessRequestImpl): SendUserAccessRequest

    @Binds
    abstract fun bindRestoreItem(impl: RestoreItemImpl): RestoreItem

    @Binds
    abstract fun bindRestoreItems(impl: RestoreItemsImpl): RestoreItems

    @Binds
    abstract fun bindDeleteItem(impl: DeleteItemImpl): DeleteItem

    @Binds
    abstract fun bindClearTrash(impl: ClearTrashImpl): ClearTrash

    @Binds
    abstract fun bindGetUserPlan(impl: GetUserPlanImpl): GetUserPlan

    @Binds
    abstract fun bindMigrateItem(impl: MigrateItemImpl): MigrateItem

    @Binds
    abstract fun bindGetVaultWithItemCountById(
        impl: GetVaultWithItemCountByIdImpl
    ): GetVaultWithItemCountById

    @Binds
    abstract fun bindCreateItemAndAlias(impl: CreateItemAndAliasImpl): CreateItemAndAlias

    @Binds
    abstract fun bindAddSearchEntry(impl: AddSearchEntryImpl): AddSearchEntry

    @Binds
    abstract fun bindDeleteSearchEntry(impl: DeleteSearchEntryImpl): DeleteSearchEntry

    @Binds
    abstract fun bindDeleteAllSearchEntry(impl: DeleteAllSearchEntryImpl): DeleteAllSearchEntry

    @Binds
    abstract fun bindObserveSearchEntry(impl: ObserveSearchEntryImpl): ObserveSearchEntry

    @Binds
    abstract fun bindGetItemByIdWithVaultImpl(impl: GetItemByIdWithVaultImpl): GetItemByIdWithVault

    @Binds
    abstract fun bindClearUserData(impl: ClearUserDataImpl): ClearUserData

    @Binds
    abstract fun bindGetUpgradeInfo(impl: ObserveUpgradeInfoImpl): ObserveUpgradeInfo

    @Binds
    abstract fun bindGetItemByAliasEmail(impl: GetItemByAliasEmailImpl): GetItemByAliasEmail

    @Binds
    abstract fun bindObserveMFACount(impl: ObserveMFACountImpl): ObserveMFACount

    @Binds
    abstract fun bindObserveVaultCount(impl: ObserveVaultCountImpl): ObserveVaultCount

    @Binds
    abstract fun bindCanDisplayTotp(impl: CanDisplayTotpImpl): CanDisplayTotp

    @Binds
    abstract fun bindCanPerformPaidAction(impl: CanPerformPaidActionImpl): CanPerformPaidAction

    @Binds
    abstract fun bindRefreshPlan(impl: RefreshPlanImpl): RefreshPlan

    @Binds
    abstract fun bindCheckMasterPassword(impl: CheckMasterPasswordImpl): CheckMasterPassword

    @Binds
    abstract fun bindObservePrimaryUserEmail(impl: ObservePrimaryUserEmailImpl): ObservePrimaryUserEmail

    @Binds
    abstract fun bindCheckPin(impl: CheckPinImpl): CheckPin

    @Binds
    abstract fun bindCreatePin(impl: CreatePinImpl): CreatePin

    @Binds
    abstract fun bindClearPin(impl: ClearPinImpl): ClearPin

    @Binds
    abstract fun bindClearData(impl: ResetAppToDefaultsImpl): ResetAppToDefaults

    @Binds
    abstract fun bindInviteToVault(impl: InviteToVaultImpl): InviteToVault

    @Binds
    abstract fun bindObserveInvites(impl: ObserveInvitesImpl): ObserveInvites

    @Binds
    abstract fun bindRefreshInvites(impl: RefreshInvitesImpl): RefreshInvites

    @Binds
    abstract fun bindPerformSync(impl: PerformSyncImpl): PerformSync

    @Binds
    abstract fun bindAcceptInvite(impl: AcceptInviteImpl): AcceptInvite

    @Binds
    abstract fun bindRejectInvite(impl: RejectInviteImpl): RejectInvite

    @Binds
    abstract fun bindLeaveVault(impl: LeaveVaultImpl): LeaveVault

    @Binds
    abstract fun bindCanShareVault(impl: CanShareVaultImpl): CanShareVault

    @Binds
    abstract fun bindCanMigrateVault(impl: CanMigrateVaultImpl): CanMigrateVault

    @Binds
    abstract fun bindCanManageVaultAccess(impl: CanManageVaultAccessImpl): CanManageVaultAccess

    @Binds
    abstract fun bindGetVaultMembers(impl: GetVaultMembersImpl): GetVaultMembers

    @Binds
    abstract fun bindRemoveMemberFromVault(impl: RemoveMemberFromVaultImpl): RemoveMemberFromVault

    @Binds
    abstract fun bindSetVaultMemberPermission(impl: SetVaultMemberPermissionImpl): SetVaultMemberPermission

    @Binds
    abstract fun bindCancelInvite(impl: CancelInviteImpl): CancelInvite

    @Binds
    abstract fun bindResendInvite(impl: ResendInviteImpl): ResendInvite

    @Binds
    abstract fun bindTransferOwnership(impl: TransferVaultOwnershipImpl): TransferVaultOwnership

    @Binds
    abstract fun bindCanCreateVault(impl: CanCreateVaultImpl): CanCreateVault

    @Binds
    abstract fun bindCanCreateItemInVault(impl: CanCreateItemInVaultImpl): CanCreateItemInVault

    @Binds
    abstract fun bindGetInviteUserMode(impl: GetInviteUserModeImpl): GetInviteUserMode

    @Binds
    abstract fun bindConfirmNewUserInvite(impl: ConfirmNewUserInviteImpl): ConfirmNewUserInvite

    @Binds
    abstract fun bindObserveUserAccessData(impl: ObserveUserAccessDataImpl): ObserveUserAccessData

    @Binds
    abstract fun bindGetAllKeysByAddress(impl: GetAllKeysByAddressImpl): GetAllKeysByAddress

    @Binds
    abstract fun bindObserveHasConfirmedInvite(impl: ObserveHasConfirmedInviteImpl): ObserveHasConfirmedInvite

    @Binds
    abstract fun bindObserveVaultById(impl: ObserveVaultByIdImpl): ObserveVaultById

    @Binds
    abstract fun bindGetItemActions(impl: GetItemActionsImpl): GetItemActions

    @Binds
    abstract fun bindGetDefaultBrowser(impl: GetDefaultBrowserImpl): GetDefaultBrowser

    @Binds
    abstract fun bindObserveDefaultVault(impl: ObserveDefaultVaultImpl): ObserveDefaultVault

    @Binds
    abstract fun bindSetDefaultVault(impl: SetDefaultVaultImpl): SetDefaultVault
}
