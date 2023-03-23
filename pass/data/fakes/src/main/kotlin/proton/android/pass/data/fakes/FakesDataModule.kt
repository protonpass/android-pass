package proton.android.pass.data.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.data.fakes.repositories.TestAliasRepository
import proton.android.pass.data.fakes.repositories.TestItemRepository
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestGetShareById
import proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestMigrateItem
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.TestTrashItem
import proton.android.pass.data.fakes.usecases.TestUpdateAlias
import proton.android.pass.data.fakes.usecases.TestUpdateAutofillItem
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.usecases.TestUpdateVault

@Module
@InstallIn(SingletonComponent::class)
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
    abstract fun bindMigrateItem(
        impl: TestMigrateItem
    ): MigrateItem

    @Binds
    abstract fun bindGetVaultWithItemCountById(
        impl: TestGetVaultWithItemCountById
    ): GetVaultWithItemCountById
}
