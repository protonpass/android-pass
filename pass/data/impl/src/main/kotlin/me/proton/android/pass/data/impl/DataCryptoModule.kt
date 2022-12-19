package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.impl.crypto.CreateItem
import me.proton.android.pass.data.impl.crypto.CreateItemImpl
import me.proton.android.pass.data.impl.crypto.OpenItem
import me.proton.android.pass.data.impl.crypto.OpenItemImpl
import me.proton.android.pass.data.impl.crypto.UpdateItem
import me.proton.android.pass.data.impl.crypto.UpdateItemImpl
import me.proton.android.pass.data.impl.crypto.context.EncryptionContextProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataCryptoModule {
    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindUpdateItem(impl: UpdateItemImpl): UpdateItem

    @Binds
    abstract fun bindOpenItem(impl: OpenItemImpl): OpenItem

    @Binds
    abstract fun bindEncryptionContextProvider(impl: EncryptionContextProviderImpl): EncryptionContextProvider
}
