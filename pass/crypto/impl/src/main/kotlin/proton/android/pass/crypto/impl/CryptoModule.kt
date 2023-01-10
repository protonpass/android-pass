package proton.android.pass.crypto.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.impl.context.EncryptionContextProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoModule {

    @Binds
    abstract fun bindEncryptionContextProvider(impl: EncryptionContextProviderImpl): EncryptionContextProvider
}
