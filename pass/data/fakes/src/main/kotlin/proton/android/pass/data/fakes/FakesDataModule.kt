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
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.FeatureFlagRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.android.pass.data.api.usecases.CreatePin
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.MarkVaultAsPrimary
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.fakes.repositories.TestAliasRepository
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.repositories.TestFeatureFlagRepository
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.fakes.usecases.TestAddSearchEntry
import proton.android.pass.data.fakes.usecases.TestApplyPendingEvents
import proton.android.pass.data.fakes.usecases.TestCanDisplayTotp
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCheckMasterPassword
import proton.android.pass.data.fakes.usecases.TestCheckPin
import proton.android.pass.data.fakes.usecases.TestResetAppToDefaults
import proton.android.pass.data.fakes.usecases.TestClearTrash
import proton.android.pass.data.fakes.usecases.TestClearUserData
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestCreateItemAndAlias
import proton.android.pass.data.fakes.usecases.TestCreatePin
import proton.android.pass.data.fakes.usecases.TestCreateVault
import proton.android.pass.data.fakes.usecases.TestDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteItem
import proton.android.pass.data.fakes.usecases.TestDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteVault
import proton.android.pass.data.fakes.usecases.TestGetAliasDetails
import proton.android.pass.data.fakes.usecases.TestGetItemByAliasEmail
import proton.android.pass.data.fakes.usecases.TestGetItemById
import proton.android.pass.data.fakes.usecases.TestGetItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestGetShareById
import proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestMarkVaultAsPrimary
import proton.android.pass.data.fakes.usecases.TestMigrateItem
import proton.android.pass.data.fakes.usecases.TestMigrateVault
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItemCount
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveMFACount
import proton.android.pass.data.fakes.usecases.TestObservePrimaryUserEmail
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.TestRefreshContent
import proton.android.pass.data.fakes.usecases.TestRefreshPlan
import proton.android.pass.data.fakes.usecases.TestRestoreItem
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItem
import proton.android.pass.data.fakes.usecases.TestUpdateAlias
import proton.android.pass.data.fakes.usecases.TestUpdateAutofillItem
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.usecases.TestUpdateVault

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
abstract class FakesDataModule {

    @Binds
    abstract fun bindGetSuggestedLoginItems(
        impl: TestGetSuggestedLoginItems
    ): GetSuggestedLoginItems

    @Binds
    abstract fun bindItemRepository(
        impl: TestItemRepository
    ): ItemRepository

    @Binds
    abstract fun bindAliasRepository(
        impl: TestAliasRepository
    ): AliasRepository

    @Binds
    abstract fun bindDraftRepository(
        impl: TestDraftRepository
    ): DraftRepository

    @Binds
    abstract fun bindCreateAlias(
        impl: TestCreateAlias
    ): CreateAlias

    @Binds
    abstract fun bindCreateItem(
        impl: TestCreateItem
    ): CreateItem

    @Binds
    abstract fun bindObserveActiveItems(
        impl: TestObserveActiveItems
    ): ObserveActiveItems

    @Binds
    abstract fun bindObserveVaults(
        impl: TestObserveVaults
    ): ObserveVaults

    @Binds
    abstract fun bindObserveCurrentUser(
        impl: TestObserveCurrentUser
    ): ObserveCurrentUser

    @Binds
    abstract fun bindObserveAliasOptions(
        impl: TestObserveAliasOptions
    ): ObserveAliasOptions

    @Binds
    abstract fun bindUpdateAlias(
        impl: TestUpdateAlias
    ): UpdateAlias

    @Binds
    abstract fun bindUpdateAutofillItem(
        impl: TestUpdateAutofillItem
    ): UpdateAutofillItem

    @Binds
    abstract fun bindUpdateItem(
        impl: TestUpdateItem
    ): UpdateItem

    @Binds
    abstract fun bindGetShareById(
        impl: TestGetShareById
    ): GetShareById

    @Binds
    abstract fun bindTrashItem(
        impl: TestTrashItem
    ): TrashItem

    @Binds
    abstract fun bindUpdateVault(
        impl: TestUpdateVault
    ): UpdateVault

    @Binds
    abstract fun bindGetVaultById(
        impl: TestGetVaultById
    ): GetVaultById

    @Binds
    abstract fun bindObserveVaultsWithItemCount(
        impl: TestObserveVaultsWithItemCount
    ): ObserveVaultsWithItemCount

    @Binds
    abstract fun bindObserveItemCount(
        impl: TestObserveItemCount
    ): ObserveItemCount

    @Binds
    abstract fun bindMigrateItem(
        impl: TestMigrateItem
    ): MigrateItem

    @Binds
    abstract fun bindGetVaultWithItemCountById(
        impl: TestGetVaultWithItemCountById
    ): GetVaultWithItemCountById

    @Binds
    abstract fun bindCreateItemAndAlias(
        impl: TestCreateItemAndAlias
    ): CreateItemAndAlias

    @Binds
    abstract fun bindDeleteVault(
        impl: TestDeleteVault
    ): DeleteVault

    @Binds
    abstract fun bindGetUserPlan(
        impl: TestGetUserPlan
    ): GetUserPlan

    @Binds
    abstract fun bindGetItemById(
        impl: TestGetItemById
    ): GetItemById

    @Binds
    abstract fun bindDeleteItem(
        impl: TestDeleteItem
    ): DeleteItem

    @Binds
    abstract fun bindRestoreItem(
        impl: TestRestoreItem
    ): RestoreItem

    @Binds
    abstract fun bindMarkVaultAsPrimary(
        impl: TestMarkVaultAsPrimary
    ): MarkVaultAsPrimary

    @Binds
    abstract fun bindRefreshContent(
        impl: TestRefreshContent
    ): RefreshContent

    @Binds
    abstract fun bindApplyPendingEvents(
        impl: TestApplyPendingEvents
    ): ApplyPendingEvents

    @Binds
    abstract fun bindRestoreItems(
        impl: TestRestoreItems
    ): RestoreItems

    @Binds
    abstract fun bindClearTrash(
        impl: TestClearTrash
    ): ClearTrash

    @Binds
    abstract fun bindAddSearchEntry(
        impl: TestAddSearchEntry
    ): AddSearchEntry

    @Binds
    abstract fun bindDeleteSearchEntry(
        impl: TestDeleteSearchEntry
    ): DeleteSearchEntry

    @Binds
    abstract fun bindDeleteAllSearchEntry(
        impl: TestDeleteAllSearchEntry
    ): DeleteAllSearchEntry

    @Binds
    abstract fun bindObserveSearchEntry(
        impl: TestObserveSearchEntry
    ): ObserveSearchEntry

    @Binds
    abstract fun bindObserveItems(
        impl: TestObserveItems
    ): ObserveItems

    @Binds
    abstract fun bindItemSyncStatusRepository(
        impl: TestItemSyncStatusRepository
    ): ItemSyncStatusRepository

    @Binds
    abstract fun bindGetItemByIdWithVault(
        impl: TestGetItemByIdWithVault
    ): GetItemByIdWithVault

    @Binds
    abstract fun bindClearUserData(
        impl: TestClearUserData
    ): ClearUserData

    @Binds
    abstract fun bindGetUpgradeInfo(
        impl: TestObserveUpgradeInfo
    ): ObserveUpgradeInfo

    @Binds
    abstract fun bindMigrateVault(
        impl: TestMigrateVault
    ): MigrateVault

    @Binds
    abstract fun bindObserveMFACount(
        impl: TestObserveMFACount
    ): ObserveMFACount

    @Binds
    abstract fun bindCreateVault(
        impl: TestCreateVault
    ): CreateVault

    @Binds
    abstract fun bindCanPerformPaidAction(
        impl: TestCanPerformPaidAction
    ): CanPerformPaidAction

    @Binds
    abstract fun bindRefreshPlan(
        impl: TestRefreshPlan
    ): RefreshPlan

    @Binds
    abstract fun bindGetAliasDetails(
        impl: TestGetAliasDetails
    ): GetAliasDetails

    @Binds
    abstract fun bindGetItemByAliasEmail(
        impl: TestGetItemByAliasEmail
    ): GetItemByAliasEmail

    @Binds
    abstract fun bindCanDisplayTotp(
        impl: TestCanDisplayTotp
    ): CanDisplayTotp

    @Binds
    abstract fun bindCheckMasterPassword(impl: TestCheckMasterPassword): CheckMasterPassword

    @Binds
    abstract fun bindObservePrimaryUserEmail(impl: TestObservePrimaryUserEmail): ObservePrimaryUserEmail

    @Binds
    abstract fun bindCheckPin(impl: TestCheckPin): CheckPin

    @Binds
    abstract fun bindCreatePin(impl: TestCreatePin): CreatePin

    @Binds
    abstract fun bindFeatureFlagRepository(impl: TestFeatureFlagRepository): FeatureFlagRepository

    @Binds
    abstract fun bindClearAppData(impl: TestResetAppToDefaults): ResetAppToDefaults
}
