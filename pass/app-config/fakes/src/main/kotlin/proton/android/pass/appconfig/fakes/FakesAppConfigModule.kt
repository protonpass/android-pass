package proton.android.pass.appconfig.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.appconfig.api.AppConfig

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesAppConfigModule {

    @Binds
    abstract fun bindAppConfig(impl: TestAppConfig): AppConfig
}
