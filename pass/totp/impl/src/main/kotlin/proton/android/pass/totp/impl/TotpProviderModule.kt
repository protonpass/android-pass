package proton.android.pass.totp.impl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import proton.android.pass.totp.api.TotpManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TotpProviderModule {

    @Provides
    @Singleton
    fun provideTotpManager(): TotpManager = TotpManagerImpl(Clock.System)
}
