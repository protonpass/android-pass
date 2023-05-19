package proton.android.pass.data.impl.migration

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigrator
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigratorImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataMigrationModule {

    @Binds
    abstract fun bindScheduler(impl: DataMigrationSchedulerImpl): DataMigrationScheduler

    @Binds
    abstract fun bindDataMigrator(impl: DataMigratorImpl): DataMigrator

    @Binds
    abstract fun bindItemHasTotpMigrator(impl: ItemHasTotpMigratorImpl): ItemHasTotpMigrator
}
