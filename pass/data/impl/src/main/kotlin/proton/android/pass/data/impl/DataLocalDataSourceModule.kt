package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.local.LocalEventDataSource
import proton.android.pass.data.impl.local.LocalEventDataSourceImpl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalItemDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.local.LocalShareDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataLocalDataSourceModule {

    @Binds
    abstract fun bindLocalItemDataSource(impl: LocalItemDataSourceImpl): LocalItemDataSource

    @Binds
    abstract fun bindLocalShareDataSource(impl: LocalShareDataSourceImpl): LocalShareDataSource

    @Binds
    abstract fun bindLocalShareKeyDataSource(impl: LocalShareKeyDataSourceImpl): LocalShareKeyDataSource

    @Binds
    abstract fun bindLocalEventDataSource(impl: LocalEventDataSourceImpl): LocalEventDataSource

}
