package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.crypto.ReencryptShareContentsImpl
import proton.android.pass.data.impl.crypto.ReencryptShareKey
import proton.android.pass.data.impl.crypto.ReencryptShareKeyImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataCryptoModule {

    @Binds
    abstract fun bindReencryptShareContents(
        impl: ReencryptShareContentsImpl
    ): ReencryptShareContents

    @Binds
    abstract fun bindReencryptShareKey(
        impl: ReencryptShareKeyImpl
    ): ReencryptShareKey
}
