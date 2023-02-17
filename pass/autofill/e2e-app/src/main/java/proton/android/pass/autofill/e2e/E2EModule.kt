package proton.android.pass.autofill.e2e

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.context.EncryptionContextProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindEncryptionContextProvider(
        impl: FakeEncryptionContextProvider
    ): EncryptionContextProvider
}
