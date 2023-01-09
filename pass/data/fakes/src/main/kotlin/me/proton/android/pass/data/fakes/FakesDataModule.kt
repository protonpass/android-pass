package me.proton.android.pass.data.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.CreateAlias
import me.proton.android.pass.data.api.usecases.CreateItem
import me.proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import me.proton.android.pass.data.api.usecases.GetShareById
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.android.pass.data.api.usecases.UpdateAlias
import me.proton.android.pass.data.api.usecases.UpdateAutofillItem
import me.proton.android.pass.data.api.usecases.UpdateItem
import me.proton.android.pass.data.fakes.repositories.TestAliasRepository
import me.proton.android.pass.data.fakes.repositories.TestItemRepository
import me.proton.android.pass.data.fakes.usecases.TestCreateAlias
import me.proton.android.pass.data.fakes.usecases.TestCreateItem
import me.proton.android.pass.data.fakes.usecases.TestGetAppNameFromPackageName
import me.proton.android.pass.data.fakes.usecases.TestGetShareById
import me.proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import me.proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import me.proton.android.pass.data.fakes.usecases.TestObserveActiveShare
import me.proton.android.pass.data.fakes.usecases.TestTrashItem
import me.proton.android.pass.data.fakes.usecases.TestUpdateAlias
import me.proton.android.pass.data.fakes.usecases.TestUpdateAutofillItem
import me.proton.android.pass.data.fakes.usecases.TestUpdateItem

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
    abstract fun bindObserveActiveShare(
        impl: TestObserveActiveShare
    ): ObserveActiveShare

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
    abstract fun bindGetAppNameFromPackageName(
        impl: TestGetAppNameFromPackageName
    ): GetAppNameFromPackageName
}
