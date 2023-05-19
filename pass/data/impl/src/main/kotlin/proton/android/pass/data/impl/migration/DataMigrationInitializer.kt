package proton.android.pass.data.impl.migration

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class DataMigrationInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                DataMigrationInitializerEntryPoint::class.java
            )
            .scheduler()
            .schedule()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DataMigrationInitializerEntryPoint {
        fun scheduler(): DataMigrationScheduler
    }
}
