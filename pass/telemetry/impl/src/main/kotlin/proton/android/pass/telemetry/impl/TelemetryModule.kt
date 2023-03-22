package proton.android.pass.telemetry.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.telemetry.impl.startup.TelemetryStartupManager
import proton.android.pass.telemetry.impl.startup.TelemetryStartupManagerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {

    @Binds
    abstract fun bindTelemetryStartupManager(
        impl: TelemetryStartupManagerImpl
    ): TelemetryStartupManager

    @Binds
    abstract fun bindTelemetryManager(impl: TelemetryManagerImpl): TelemetryManager
}

