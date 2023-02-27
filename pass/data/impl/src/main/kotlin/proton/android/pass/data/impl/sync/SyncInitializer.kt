package proton.android.pass.data.impl.sync

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SyncInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                SyncInitializerEntryPoint::class.java
            )
            .syncManager()
            .start()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncInitializerEntryPoint {
        fun syncManager(): SyncManager
    }
}
