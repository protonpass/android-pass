package proton.android.pass.telemetry.impl.startup

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class TelemetryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                TelemetryInitializerEntryPoint::class.java
            )
            .telemetryManager()
            .start()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TelemetryInitializerEntryPoint {
        fun telemetryManager(): TelemetryStartupManager
    }
}
