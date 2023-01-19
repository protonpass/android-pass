package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetAddressById
import proton.android.pass.data.api.usecases.GetAddressesForUserId
import proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import proton.android.pass.data.api.usecases.GetCurrentShare
import proton.android.pass.data.api.usecases.GetCurrentUserId
import proton.android.pass.data.api.usecases.GetPublicSuffixList
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveAccounts
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveTrashedItems
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshShares
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.UpdateActiveShare
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionItemFiltererImpl
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.data.impl.autofill.SuggestionSorterImpl
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.data.impl.usecases.ApplyPendingEventsImpl
import proton.android.pass.data.impl.usecases.CreateAliasImpl
import proton.android.pass.data.impl.usecases.CreateItemImpl
import proton.android.pass.data.impl.usecases.CreateVaultImpl
import proton.android.pass.data.impl.usecases.DeleteVaultImpl
import proton.android.pass.data.impl.usecases.GetAddressByIdImpl
import proton.android.pass.data.impl.usecases.GetAddressesForUserIdImpl
import proton.android.pass.data.impl.usecases.GetAppNameFromPackageNameImpl
import proton.android.pass.data.impl.usecases.GetCurrentShareImpl
import proton.android.pass.data.impl.usecases.GetCurrentUserIdImpl
import proton.android.pass.data.impl.usecases.GetPublicSuffixListImpl
import proton.android.pass.data.impl.usecases.GetShareByIdImpl
import proton.android.pass.data.impl.usecases.GetSuggestedLoginItemsImpl
import proton.android.pass.data.impl.usecases.MigrateVaultImpl
import proton.android.pass.data.impl.usecases.ObserveAccountsImpl
import proton.android.pass.data.impl.usecases.ObserveActiveItemsImpl
import proton.android.pass.data.impl.usecases.ObserveActiveShareIdImpl
import proton.android.pass.data.impl.usecases.ObserveAllSharesImpl
import proton.android.pass.data.impl.usecases.ObserveCurrentUserImpl
import proton.android.pass.data.impl.usecases.ObserveItemsImpl
import proton.android.pass.data.impl.usecases.ObserveTrashedItemsImpl
import proton.android.pass.data.impl.usecases.RefreshContentImpl
import proton.android.pass.data.impl.usecases.RefreshSharesImpl
import proton.android.pass.data.impl.usecases.TrashItemImpl
import proton.android.pass.data.impl.usecases.UpdateActiveShareImpl
import proton.android.pass.data.impl.usecases.UpdateAliasImpl
import proton.android.pass.data.impl.usecases.UpdateAutofillItemImpl
import proton.android.pass.data.impl.usecases.UpdateItemImpl

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
    abstract fun bindObserveActiveShare(impl: ObserveActiveShareIdImpl): ObserveActiveShare

    @Binds
    abstract fun bindObserveCurrentUser(impl: ObserveCurrentUserImpl): ObserveCurrentUser

    @Binds
    abstract fun bindUpdateActiveShare(impl: UpdateActiveShareImpl): UpdateActiveShare

    @Binds
    abstract fun bindObserveItems(impl: ObserveItemsImpl): ObserveItems

    @Binds
    abstract fun bindObserveShares(impl: ObserveAllSharesImpl): ObserveAllShares

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

    @Binds
    abstract fun bindGetPublicSuffixList(impl: GetPublicSuffixListImpl): GetPublicSuffixList

    @Binds
    abstract fun bindSuggestionItemFilterer(impl: SuggestionItemFiltererImpl): SuggestionItemFilterer

    @Binds
    abstract fun bindHostParser(impl: HostParserImpl): HostParser

    @Binds
    abstract fun bindSuggestionSorter(impl: SuggestionSorterImpl): SuggestionSorter
}

