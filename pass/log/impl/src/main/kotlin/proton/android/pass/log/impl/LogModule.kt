package proton.android.pass.log.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.log.api.LogSharing

@Module
@InstallIn(SingletonComponent::class)
abstract class LogModule {

    @Binds
    abstract fun bindLogSharing(impl: InternalLogSharing): LogSharing
}
