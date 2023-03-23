package proton.android.pass.crypto.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.android.pass.crypto.impl.usecases.CreateItemImpl
import proton.android.pass.crypto.impl.usecases.CreateVaultImpl
import proton.android.pass.crypto.impl.usecases.MigrateItemImpl
import proton.android.pass.crypto.impl.usecases.OpenItemImpl
import proton.android.pass.crypto.impl.usecases.OpenItemKeyImpl
import proton.android.pass.crypto.impl.usecases.UpdateItemImpl
import proton.android.pass.crypto.impl.usecases.UpdateVaultImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindCreateVault(impl: CreateVaultImpl): CreateVault

    @Binds
    abstract fun bindOpenItem(impl: OpenItemImpl): OpenItem

    @Binds
    abstract fun bindUpdateItem(impl: UpdateItemImpl): UpdateItem

    @Binds
    abstract fun bindOpenItemKey(impl: OpenItemKeyImpl): OpenItemKey

    @Binds
    abstract fun bindUpdateVault(impl: UpdateVaultImpl): UpdateVault

    @Binds
    abstract fun bindMigrateItem(impl: MigrateItemImpl): MigrateItem

}
