package proton.android.pass.telemetry.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.telemetry.api.TelemetryManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesTelemetryModule {

    @Binds
    abstract fun bindTelemetryManager(impl: TestTelemetryManager): TelemetryManager
}

