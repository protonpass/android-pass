package me.proton.pass.domain

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.pass.domain.usecases.CreateItem
import me.proton.pass.domain.usecases.CreateItemImpl
import me.proton.pass.domain.usecases.GetCurrentShare
import me.proton.pass.domain.usecases.GetCurrentShareImpl
import me.proton.pass.domain.usecases.GetCurrentUserId
import me.proton.pass.domain.usecases.GetCurrentUserIdImpl
import me.proton.pass.domain.usecases.GetSuggestedLoginItems
import me.proton.pass.domain.usecases.GetSuggestedLoginItemsImpl
import me.proton.pass.domain.usecases.ObserveActiveItems
import me.proton.pass.domain.usecases.ObserveActiveItemsImpl
import me.proton.pass.domain.usecases.ObserveActiveShare
import me.proton.pass.domain.usecases.ObserveActiveShareImpl
import me.proton.pass.domain.usecases.RefreshContent
import me.proton.pass.domain.usecases.RefreshContentImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindCreateItem(createItemImpl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindObserveActiveItems(observeActiveItemsImpl: ObserveActiveItemsImpl): ObserveActiveItems

    @Binds
    abstract fun bindObserveActiveShare(observeActiveShareImpl: ObserveActiveShareImpl): ObserveActiveShare

    @Binds
    abstract fun bindRefreshContent(refreshContentImpl: RefreshContentImpl): RefreshContent

    @Binds
    abstract fun bindGetCurrentUserId(getCurrentUserIdImpl: GetCurrentUserIdImpl): GetCurrentUserId

    @Binds
    abstract fun bindGetCurrentShare(getCurrentShareImpl: GetCurrentShareImpl): GetCurrentShare

    @Binds
    abstract fun bindGetSuggestedLoginItems(
        getSuggestedLoginItemsImpl: GetSuggestedLoginItemsImpl
    ): GetSuggestedLoginItems
}
