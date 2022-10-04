package me.proton.core.pass.di.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.pass.domain.usecases.CreateItem
import me.proton.core.pass.domain.usecases.CreateItemImpl
import me.proton.core.pass.domain.usecases.ObserveActiveItems
import me.proton.core.pass.domain.usecases.ObserveActiveItemsImpl
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.domain.usecases.ObserveActiveShareImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindCreateItem(createItemImpl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindObserveActiveItems(observeActiveItemsImpl: ObserveActiveItemsImpl): ObserveActiveItems

    @Binds
    abstract fun bindObserveActiveShare(observeActiveShareImpl: ObserveActiveShareImpl): ObserveActiveShare
}
