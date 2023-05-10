package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.local.LocalEventDataSource
import proton.android.pass.data.impl.local.LocalEventDataSourceImpl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalItemDataSourceImpl
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.local.LocalPlanDataSourceImpl
import proton.android.pass.data.impl.local.LocalSearchEntryDataSource
import proton.android.pass.data.impl.local.LocalSearchEntryDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.local.LocalShareDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSourceImpl
import proton.android.pass.data.impl.local.LocalTelemetryDataSource
import proton.android.pass.data.impl.local.LocalTelemetryDataSourceImpl

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

    @Binds
    abstract fun bindLocalTelemetryDataSource(impl: LocalTelemetryDataSourceImpl): LocalTelemetryDataSource

    @Binds
    abstract fun bindLocalSearchEntryDataSource(impl: LocalSearchEntryDataSourceImpl): LocalSearchEntryDataSource

    @Binds
    abstract fun bindLocalPlanLimitsDataSource(impl: LocalPlanDataSourceImpl): LocalPlanDataSource
}
