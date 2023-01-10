package proton.android.pass.crypto.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesCryptoModule {

    @Binds
    abstract fun bindEncryptionContextProvider(impl: TestEncryptionContextProvider): EncryptionContextProvider
}
