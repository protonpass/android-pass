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

package proton.android.pass.data.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.core.repositories.SentinelRepository
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CancelInvite
import proton.android.pass.data.api.usecases.CheckCanAddressesBeInvited
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
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.api.usecases.GetDefaultBrowser
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedCreditCardItems
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.LeaveVault
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAddressesByUserId
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveAppNeedsUpdate
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveCurrentUserSettings
import proton.android.pass.data.api.usecases.ObserveHasConfirmedInvite
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveUsableVaults
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.data.api.usecases.ObserveVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.PinItems
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.data.api.usecases.RemoveMemberFromVault
import proton.android.pass.data.api.usecases.ResendInvite
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.api.usecases.RestoreAllItems
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.SetVaultMemberPermission
import proton.android.pass.data.api.usecases.TransferVaultOwnership
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.UnpinItems
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.data.api.usecases.breach.AddBreachCustomEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreach
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForProtonEmail
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.data.api.usecases.breach.VerifyBreachCustomEmail
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.data.api.usecases.items.GetItemCategory
import proton.android.pass.data.api.usecases.items.ObserveItemRevisions
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.fakes.repositories.FakeSentinelRepository
import proton.android.pass.data.fakes.repositories.TestAliasRepository
import proton.android.pass.data.fakes.repositories.TestBulkInviteRepository
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.repositories.TestInviteRepository
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.fakes.repositories.TestUserAccessDataRepository
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeObserveAddressesByUserId
import proton.android.pass.data.fakes.usecases.FakeObserveInviteRecommendations
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.TestAcceptInvite
import proton.android.pass.data.fakes.usecases.TestAddSearchEntry
import proton.android.pass.data.fakes.usecases.TestApplyPendingEvents
import proton.android.pass.data.fakes.usecases.TestCanCreateItemInVault
import proton.android.pass.data.fakes.usecases.TestCanCreateVault
import proton.android.pass.data.fakes.usecases.TestCanDisplayTotp
import proton.android.pass.data.fakes.usecases.TestCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.TestCanMigrateVault
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCanShareVault
import proton.android.pass.data.fakes.usecases.TestCancelInvite
import proton.android.pass.data.fakes.usecases.TestCheckAddressesCanBeInvited
import proton.android.pass.data.fakes.usecases.TestCheckMasterPassword
import proton.android.pass.data.fakes.usecases.TestCheckPin
import proton.android.pass.data.fakes.usecases.TestClearPin
import proton.android.pass.data.fakes.usecases.TestClearTrash
import proton.android.pass.data.fakes.usecases.TestClearUserData
import proton.android.pass.data.fakes.usecases.TestConfirmNewUserInvite
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestCreateItemAndAlias
import proton.android.pass.data.fakes.usecases.TestCreatePin
import proton.android.pass.data.fakes.usecases.TestCreateVault
import proton.android.pass.data.fakes.usecases.TestDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteItems
import proton.android.pass.data.fakes.usecases.TestDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteVault
import proton.android.pass.data.fakes.usecases.TestGetAliasDetails
import proton.android.pass.data.fakes.usecases.TestGetAllKeysByAddress
import proton.android.pass.data.fakes.usecases.TestGetDefaultBrowser
import proton.android.pass.data.fakes.usecases.TestGetInviteUserMode
import proton.android.pass.data.fakes.usecases.TestGetItemActions
import proton.android.pass.data.fakes.usecases.TestGetItemByAliasEmail
import proton.android.pass.data.fakes.usecases.TestGetItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestGetPasskeyById
import proton.android.pass.data.fakes.usecases.TestGetShareById
import proton.android.pass.data.fakes.usecases.TestGetSuggestedCreditCardItems
import proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.data.fakes.usecases.TestGetVaultMembers
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestInviteToVault
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestLeaveVault
import proton.android.pass.data.fakes.usecases.TestMigrateItems
import proton.android.pass.data.fakes.usecases.TestMigrateVault
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveAppNeedsUpdate
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUserSettings
import proton.android.pass.data.fakes.usecases.TestObserveDefaultVault
import proton.android.pass.data.fakes.usecases.TestObserveHasConfirmedInvite
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItemCount
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveItemsWithPasskeys
import proton.android.pass.data.fakes.usecases.TestObserveMFACount
import proton.android.pass.data.fakes.usecases.TestObserveOrganizationSettings
import proton.android.pass.data.fakes.usecases.TestObservePinnedItems
import proton.android.pass.data.fakes.usecases.TestObservePrimaryUserEmail
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveUsableVaults
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.TestObserveVaultById
import proton.android.pass.data.fakes.usecases.TestObserveVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.TestPerformSync
import proton.android.pass.data.fakes.usecases.TestPinItems
import proton.android.pass.data.fakes.usecases.TestRefreshContent
import proton.android.pass.data.fakes.usecases.TestRefreshInvites
import proton.android.pass.data.fakes.usecases.TestRefreshPlan
import proton.android.pass.data.fakes.usecases.TestRejectInvite
import proton.android.pass.data.fakes.usecases.TestRemoveMemberFromVault
import proton.android.pass.data.fakes.usecases.TestResendInvite
import proton.android.pass.data.fakes.usecases.TestResetAppToDefaults
import proton.android.pass.data.fakes.usecases.TestRestoreAllItems
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestSetDefaultVault
import proton.android.pass.data.fakes.usecases.TestSetVaultMemberPermission
import proton.android.pass.data.fakes.usecases.TestTransferVaultOwnership
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.data.fakes.usecases.TestUnpinItems
import proton.android.pass.data.fakes.usecases.TestUpdateAlias
import proton.android.pass.data.fakes.usecases.TestUpdateAutofillItem
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.usecases.TestUpdateVault
import proton.android.pass.data.fakes.usecases.breach.FakeAddBreachCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreach
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachCustomEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForAliasEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForProtonEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveCustomEmailSuggestions
import proton.android.pass.data.fakes.usecases.breach.FakeVerifyBreachCustomEmail
import proton.android.pass.data.fakes.usecases.items.FakeGetItemCategory
import proton.android.pass.data.fakes.usecases.items.FakeObserveItemRevisions
import proton.android.pass.data.fakes.usecases.items.FakeOpenItemRevision
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
abstract class FakesDataModule {

    @Binds
    abstract fun bindGetSuggestedLoginItems(impl: TestGetSuggestedLoginItems): GetSuggestedLoginItems

    @Binds
    abstract fun bindGetSuggestedCreditCardItems(impl: TestGetSuggestedCreditCardItems): GetSuggestedCreditCardItems

    @Binds
    abstract fun bindItemRepository(impl: TestItemRepository): ItemRepository

    @Binds
    abstract fun bindAliasRepository(impl: TestAliasRepository): AliasRepository

    @Binds
    abstract fun bindDraftRepository(impl: TestDraftRepository): DraftRepository

    @Binds
    abstract fun bindCreateAlias(impl: TestCreateAlias): CreateAlias

    @Binds
    abstract fun bindCreateItem(impl: TestCreateItem): CreateItem

    @Binds
    abstract fun bindObserveActiveItems(impl: TestObserveActiveItems): ObserveActiveItems

    @Binds
    abstract fun bindObserveVaults(impl: TestObserveVaults): ObserveVaults

    @Binds
    abstract fun bindObserveCurrentUser(impl: TestObserveCurrentUser): ObserveCurrentUser

    @Binds
    abstract fun bindObserveCurrentUserSettings(impl: TestObserveCurrentUserSettings): ObserveCurrentUserSettings

    @Binds
    abstract fun bindObserveAliasOptions(impl: TestObserveAliasOptions): ObserveAliasOptions

    @Binds
    abstract fun bindUpdateAlias(impl: TestUpdateAlias): UpdateAlias

    @Binds
    abstract fun bindUpdateAutofillItem(impl: TestUpdateAutofillItem): UpdateAutofillItem

    @Binds
    abstract fun bindUpdateItem(impl: TestUpdateItem): UpdateItem

    @Binds
    abstract fun bindGetShareById(impl: TestGetShareById): GetShareById

    @Binds
    abstract fun bindTrashItem(impl: TestTrashItems): TrashItems

    @Binds
    abstract fun bindUpdateVault(impl: TestUpdateVault): UpdateVault

    @Binds
    abstract fun bindGetVaultById(impl: TestGetVaultById): GetVaultById

    @Binds
    abstract fun bindObserveVaultsWithItemCount(impl: TestObserveVaultsWithItemCount): ObserveVaultsWithItemCount

    @Binds
    abstract fun bindObserveItemCount(impl: TestObserveItemCount): ObserveItemCount

    @Binds
    abstract fun bindMigrateItem(impl: TestMigrateItems): MigrateItems

    @Binds
    abstract fun bindGetVaultWithItemCountById(impl: TestGetVaultWithItemCountById): GetVaultWithItemCountById

    @Binds
    abstract fun bindCreateItemAndAlias(impl: TestCreateItemAndAlias): CreateItemAndAlias

    @Binds
    abstract fun bindDeleteVault(impl: TestDeleteVault): DeleteVault

    @Binds
    abstract fun bindGetUserPlan(impl: TestGetUserPlan): GetUserPlan


    @Binds
    abstract fun bindGetItemById(impl: FakeGetItemById): GetItemById

    @Binds
    abstract fun bindObserveItemById(impl: TestObserveItemById): ObserveItemById

    @Binds
    abstract fun bindDeleteItem(impl: TestDeleteItems): DeleteItems

    @Binds
    abstract fun bindRestoreItem(impl: TestRestoreItems): RestoreItems

    @Binds
    abstract fun bindRefreshContent(impl: TestRefreshContent): RefreshContent

    @Binds
    abstract fun bindApplyPendingEvents(impl: TestApplyPendingEvents): ApplyPendingEvents

    @Binds
    abstract fun bindRestoreItems(impl: TestRestoreAllItems): RestoreAllItems

    @Binds
    abstract fun bindClearTrash(impl: TestClearTrash): ClearTrash

    @Binds
    abstract fun bindAddSearchEntry(impl: TestAddSearchEntry): AddSearchEntry

    @Binds
    abstract fun bindDeleteSearchEntry(impl: TestDeleteSearchEntry): DeleteSearchEntry

    @Binds
    abstract fun bindDeleteAllSearchEntry(impl: TestDeleteAllSearchEntry): DeleteAllSearchEntry

    @Binds
    abstract fun bindObserveSearchEntry(impl: TestObserveSearchEntry): ObserveSearchEntry

    @Binds
    abstract fun bindObserveItems(impl: TestObserveItems): ObserveItems

    @Binds
    abstract fun bindObservePinnedItems(impl: TestObservePinnedItems): ObservePinnedItems

    @Binds
    abstract fun bindItemSyncStatusRepository(impl: TestItemSyncStatusRepository): ItemSyncStatusRepository

    @Binds
    abstract fun bindGetItemByIdWithVault(impl: TestGetItemByIdWithVault): GetItemByIdWithVault

    @Binds
    abstract fun bindClearUserData(impl: TestClearUserData): ClearUserData

    @Binds
    abstract fun bindGetUpgradeInfo(impl: TestObserveUpgradeInfo): ObserveUpgradeInfo

    @Binds
    abstract fun bindMigrateVault(impl: TestMigrateVault): MigrateVault

    @Binds
    abstract fun bindObserveMFACount(impl: TestObserveMFACount): ObserveMFACount

    @Binds
    abstract fun bindCreateVault(impl: TestCreateVault): CreateVault

    @Binds
    abstract fun bindCanPerformPaidAction(impl: TestCanPerformPaidAction): CanPerformPaidAction

    @Binds
    abstract fun bindRefreshPlan(impl: TestRefreshPlan): RefreshPlan

    @Binds
    abstract fun bindGetAliasDetails(impl: TestGetAliasDetails): GetAliasDetails

    @Binds
    abstract fun bindGetItemByAliasEmail(impl: TestGetItemByAliasEmail): GetItemByAliasEmail

    @Binds
    abstract fun bindCanDisplayTotp(impl: TestCanDisplayTotp): CanDisplayTotp

    @Binds
    abstract fun bindCheckMasterPassword(impl: TestCheckMasterPassword): CheckMasterPassword

    @Binds
    abstract fun bindObservePrimaryUserEmail(impl: TestObservePrimaryUserEmail): ObservePrimaryUserEmail

    @Binds
    abstract fun bindCheckPin(impl: TestCheckPin): CheckPin

    @Binds
    abstract fun bindCreatePin(impl: TestCreatePin): CreatePin

    @Binds
    abstract fun bindClearAppData(impl: TestResetAppToDefaults): ResetAppToDefaults

    @Binds
    abstract fun bindClearPin(impl: TestClearPin): ClearPin

    @Binds
    abstract fun bindInviteToVault(impl: TestInviteToVault): InviteToVault

    @Binds
    abstract fun bindInviteRepository(impl: TestInviteRepository): InviteRepository

    @Binds
    abstract fun bindObserveInvites(impl: TestObserveInvites): ObserveInvites

    @Binds
    abstract fun bindObserveInviteRecommendations(impl: FakeObserveInviteRecommendations): ObserveInviteRecommendations

    @Binds
    abstract fun bindRefreshInvites(impl: TestRefreshInvites): RefreshInvites

    @Binds
    abstract fun bindPerformSync(impl: TestPerformSync): PerformSync

    @Binds
    abstract fun bindAcceptInvite(impl: TestAcceptInvite): AcceptInvite

    @Binds
    abstract fun bindRejectInvite(impl: TestRejectInvite): RejectInvite

    @Binds
    abstract fun bindLeaveVault(impl: TestLeaveVault): LeaveVault

    @Binds
    abstract fun bindCanShareVault(impl: TestCanShareVault): CanShareVault

    @Binds
    abstract fun bindCanMigrateVault(impl: TestCanMigrateVault): CanMigrateVault

    @Binds
    abstract fun bindCanManageVaultAccess(impl: TestCanManageVaultAccess): CanManageVaultAccess

    @Binds
    abstract fun bindGetVaultMembers(impl: TestGetVaultMembers): GetVaultMembers

    @Binds
    abstract fun bindRemoveMemberFromVault(impl: TestRemoveMemberFromVault): RemoveMemberFromVault

    @Binds
    abstract fun bindSetVaultMemberPermission(impl: TestSetVaultMemberPermission): SetVaultMemberPermission

    @Binds
    abstract fun bindResendInvite(impl: TestResendInvite): ResendInvite

    @Binds
    abstract fun bindCancelInvite(impl: TestCancelInvite): CancelInvite

    @Binds
    abstract fun bindTransferOwnership(impl: TestTransferVaultOwnership): TransferVaultOwnership

    @Binds
    abstract fun bindCanCreateVault(impl: TestCanCreateVault): CanCreateVault

    @Binds
    abstract fun bindCanCreateItemInVault(impl: TestCanCreateItemInVault): CanCreateItemInVault

    @Binds
    abstract fun bindGetInviteUserMode(impl: TestGetInviteUserMode): GetInviteUserMode

    @Binds
    abstract fun bindConfirmNewUserInvite(impl: TestConfirmNewUserInvite): ConfirmNewUserInvite

    @Binds
    abstract fun bindUserAccessDataRepository(impl: TestUserAccessDataRepository): UserAccessDataRepository

    @Binds
    abstract fun bindObserveUserAccessData(impl: TestObserveUserAccessData): ObserveUserAccessData

    @Binds
    abstract fun bindGetAllKeysByAddress(impl: TestGetAllKeysByAddress): GetAllKeysByAddress

    @Binds
    abstract fun bindObserveHasConfirmedInvite(impl: TestObserveHasConfirmedInvite): ObserveHasConfirmedInvite

    @Binds
    abstract fun bindObserveVaultById(impl: TestObserveVaultById): ObserveVaultById

    @Binds
    abstract fun bindGetItemActions(impl: TestGetItemActions): GetItemActions

    @Binds
    abstract fun bindGetDefaultBrowser(impl: TestGetDefaultBrowser): GetDefaultBrowser

    @Binds
    abstract fun bindObserveDefaultVault(impl: TestObserveDefaultVault): ObserveDefaultVault

    @Binds
    abstract fun bindSetDefaultVault(impl: TestSetDefaultVault): SetDefaultVault

    @Binds
    abstract fun bindBulkMoveToVaultRepository(impl: TestBulkMoveToVaultRepository): BulkMoveToVaultRepository

    @Binds
    abstract fun bindPinItem(impl: FakePinItem): PinItem

    @Binds
    abstract fun bindUnpinItem(impl: FakeUnpinItem): UnpinItem

    @Binds
    abstract fun bindPinItems(impl: TestPinItems): PinItems

    @Binds
    abstract fun bindUnpinItems(impl: TestUnpinItems): UnpinItems

    @Binds
    abstract fun bindBulkInviteRepository(impl: TestBulkInviteRepository): BulkInviteRepository

    @Binds
    abstract fun bindObserveAppNeedsUpdate(impl: TestObserveAppNeedsUpdate): ObserveAppNeedsUpdate

    @Binds
    abstract fun bindObserveVaultWithItemCountById(
        impl: TestObserveVaultWithItemCountById
    ): ObserveVaultWithItemCountById

    @Binds
    abstract fun bindObserveItemRevisions(impl: FakeObserveItemRevisions): ObserveItemRevisions

    @Binds
    abstract fun bindGetItemCategory(impl: FakeGetItemCategory): GetItemCategory

    @Binds
    abstract fun bindObserveOrganizationSettings(impl: TestObserveOrganizationSettings): ObserveOrganizationSettings

    @Binds
    abstract fun bindCheckCanAddressesBeInvited(impl: TestCheckAddressesCanBeInvited): CheckCanAddressesBeInvited

    @Binds
    abstract fun bindObserveUsableVaults(impl: TestObserveUsableVaults): ObserveUsableVaults

    @Binds
    abstract fun bindObserveItemsWithPasskeys(impl: TestObserveItemsWithPasskeys): ObserveItemsWithPasskeys

    @Binds
    abstract fun bindGetPasskeyById(impl: TestGetPasskeyById): GetPasskeyById

    @Binds
    abstract fun bindOpenItemRevision(impl: FakeOpenItemRevision): OpenItemRevision

    @Binds
    abstract fun bindSentinelRepository(impl: FakeSentinelRepository): SentinelRepository

    @[Binds Singleton]
    abstract fun bindObserveBreach(impl: FakeObserveBreach): ObserveBreach

    @Binds
    abstract fun bindObserveBreachCustomEmails(impl: FakeObserveBreachCustomEmails): ObserveBreachCustomEmails

    @Binds
    abstract fun bindAddBreachCustomEmail(impl: FakeAddBreachCustomEmail): AddBreachCustomEmail

    @Binds
    abstract fun bindVerifyBreachCustomEmail(impl: FakeVerifyBreachCustomEmail): VerifyBreachCustomEmail

    @Binds
    abstract fun bindObserveBreachesForProtonEmail(
        impl: FakeObserveBreachesForProtonEmail
    ): ObserveBreachesForProtonEmail

    @Binds
    abstract fun bindObserveBreachesForCustomEmail(
        impl: FakeObserveBreachesForCustomEmail
    ): ObserveBreachesForCustomEmail

    @Binds
    abstract fun bindObserveBreachesForAlias(impl: FakeObserveBreachesForAliasEmail): ObserveBreachesForAliasEmail

    @Binds
    abstract fun bindObserveCustomEmailSuggestions(
        impl: FakeObserveCustomEmailSuggestions
    ): ObserveCustomEmailSuggestions

    @Binds
    abstract fun bindObserveAddressesByUserId(impl: FakeObserveAddressesByUserId): ObserveAddressesByUserId
}
