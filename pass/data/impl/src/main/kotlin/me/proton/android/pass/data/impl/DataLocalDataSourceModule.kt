package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.impl.local.LocalItemDataSource
import me.proton.android.pass.data.impl.local.LocalItemDataSourceImpl
import me.proton.android.pass.data.impl.local.LocalShareDataSource
import me.proton.android.pass.data.impl.local.LocalShareDataSourceImpl
import me.proton.android.pass.data.impl.local.LocalVaultItemKeyDataSource
import me.proton.android.pass.data.impl.local.LocalVaultItemKeyDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataLocalDataSourceModule {

    @Binds
    abstract fun bindLocalItemDataSource(impl: LocalItemDataSourceImpl): LocalItemDataSource

    @Binds
    abstract fun bindLocalShareDataSource(impl: LocalShareDataSourceImpl): LocalShareDataSource

    @Binds
    abstract fun bindLocalVaultItemKeyDataSource(impl: LocalVaultItemKeyDataSourceImpl): LocalVaultItemKeyDataSource

}
