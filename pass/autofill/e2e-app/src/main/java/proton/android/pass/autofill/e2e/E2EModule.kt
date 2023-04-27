package proton.android.pass.autofill.e2e

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock

@Module
@InstallIn(SingletonComponent::class)
object E2EModule {

    @Provides
    fun provideClock(): Clock = Clock.System
}
