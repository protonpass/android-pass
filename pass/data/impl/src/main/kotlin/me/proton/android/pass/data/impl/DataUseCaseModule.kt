package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.api.usecases.ApplyPendingEvents
import me.proton.android.pass.data.api.usecases.CreateAlias
import me.proton.android.pass.data.api.usecases.CreateItem
import me.proton.android.pass.data.api.usecases.CreateVault
import me.proton.android.pass.data.api.usecases.GetAddressById
import me.proton.android.pass.data.api.usecases.GetAddressesForUserId
import me.proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import me.proton.android.pass.data.api.usecases.GetCurrentShare
import me.proton.android.pass.data.api.usecases.GetCurrentUserId
import me.proton.android.pass.data.api.usecases.GetShareById
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import me.proton.android.pass.data.api.usecases.ObserveAccounts
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.android.pass.data.api.usecases.ObserveCurrentUser
import me.proton.android.pass.data.api.usecases.ObserveItems
import me.proton.android.pass.data.api.usecases.ObserveShares
import me.proton.android.pass.data.api.usecases.ObserveTrashedItems
import me.proton.android.pass.data.api.usecases.RefreshContent
import me.proton.android.pass.data.api.usecases.RefreshShares
import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.android.pass.data.api.usecases.UpdateAlias
import me.proton.android.pass.data.api.usecases.UpdateAutofillItem
import me.proton.android.pass.data.impl.usecases.ApplyPendingEventsImpl
import me.proton.android.pass.data.impl.usecases.CreateAliasImpl
import me.proton.android.pass.data.impl.usecases.CreateItemImpl
import me.proton.android.pass.data.impl.usecases.CreateVaultImpl
import me.proton.android.pass.data.impl.usecases.GetAddressByIdImpl
import me.proton.android.pass.data.impl.usecases.GetAddressesForUserIdImpl
import me.proton.android.pass.data.impl.usecases.GetAppNameFromPackageNameImpl
import me.proton.android.pass.data.impl.usecases.GetCurrentShareImpl
import me.proton.android.pass.data.impl.usecases.GetCurrentUserIdImpl
import me.proton.android.pass.data.impl.usecases.GetShareByIdImpl
import me.proton.android.pass.data.impl.usecases.GetSuggestedLoginItemsImpl
import me.proton.android.pass.data.impl.usecases.ObserveAccountsImpl
import me.proton.android.pass.data.impl.usecases.ObserveActiveItemsImpl
import me.proton.android.pass.data.impl.usecases.ObserveActiveShareImpl
import me.proton.android.pass.data.impl.usecases.ObserveCurrentUserImpl
import me.proton.android.pass.data.impl.usecases.ObserveItemsImpl
import me.proton.android.pass.data.impl.usecases.ObserveSharesImpl
import me.proton.android.pass.data.impl.usecases.ObserveTrashedItemsImpl
import me.proton.android.pass.data.impl.usecases.RefreshContentImpl
import me.proton.android.pass.data.impl.usecases.RefreshSharesImpl
import me.proton.android.pass.data.impl.usecases.TrashItemImpl
import me.proton.android.pass.data.impl.usecases.UpdateAliasImpl
import me.proton.android.pass.data.impl.usecases.UpdateAutofillItemImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataUseCaseModule {

    @Binds
    abstract fun bindCreateAlias(impl: CreateAliasImpl): CreateAlias

    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindCreateVault(impl: CreateVaultImpl): CreateVault

    @Binds
    abstract fun bindGetAddressById(impl: GetAddressByIdImpl): GetAddressById

    @Binds
    abstract fun bindGetAddressesForUserId(impl: GetAddressesForUserIdImpl): GetAddressesForUserId

    @Binds
    abstract fun bindGetCurrentShare(impl: GetCurrentShareImpl): GetCurrentShare

    @Binds
    abstract fun bindGetCurrentUserId(impl: GetCurrentUserIdImpl): GetCurrentUserId

    @Binds
    abstract fun bindGetShareById(impl: GetShareByIdImpl): GetShareById

    @Binds
    abstract fun bindGetSuggestedLoginItems(impl: GetSuggestedLoginItemsImpl): GetSuggestedLoginItems

    @Binds
    abstract fun bindObserveAccounts(impl: ObserveAccountsImpl): ObserveAccounts

    @Binds
    abstract fun bindObserveActiveItems(impl: ObserveActiveItemsImpl): ObserveActiveItems

    @Binds
    abstract fun bindObserveActiveShare(impl: ObserveActiveShareImpl): ObserveActiveShare

    @Binds
    abstract fun bindObserveCurrentUser(impl: ObserveCurrentUserImpl): ObserveCurrentUser

    @Binds
    abstract fun bindObserveItems(impl: ObserveItemsImpl): ObserveItems

    @Binds
    abstract fun bindObserveShares(impl: ObserveSharesImpl): ObserveShares

    @Binds
    abstract fun bindObserveTrashedItems(impl: ObserveTrashedItemsImpl): ObserveTrashedItems

    @Binds
    abstract fun bindRefreshContent(impl: RefreshContentImpl): RefreshContent

    @Binds
    abstract fun bindRefreshShares(impl: RefreshSharesImpl): RefreshShares

    @Binds
    abstract fun bindTrashItem(impl: TrashItemImpl): TrashItem

    @Binds
    abstract fun bindUpdateAlias(impl: UpdateAliasImpl): UpdateAlias

    @Binds
    abstract fun bindUpdateAutofillItem(impl: UpdateAutofillItemImpl): UpdateAutofillItem

    @Binds
    abstract fun bindApplyPendingEvents(impl: ApplyPendingEventsImpl): ApplyPendingEvents

    @Binds
    abstract fun bindGetAppNameFromPackageName(
        impl: GetAppNameFromPackageNameImpl
    ): GetAppNameFromPackageName
}

