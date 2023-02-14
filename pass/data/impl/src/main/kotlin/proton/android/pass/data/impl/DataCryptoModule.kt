package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.crypto.ReencryptShareContentsImpl
import proton.android.pass.data.impl.crypto.ReencryptShareEntityContents
import proton.android.pass.data.impl.crypto.ReencryptShareEntityContentsImpl
import proton.android.pass.data.impl.crypto.ShareEntityToShare
import proton.android.pass.data.impl.crypto.ShareEntityToShareImpl
import proton.android.pass.data.impl.crypto.ShareResponseToEntity
import proton.android.pass.data.impl.crypto.ShareResponseToEntityImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataCryptoModule {

    @Binds
    abstract fun bindShareEntityToShare(impl: ShareEntityToShareImpl): ShareEntityToShare

    @Binds
    abstract fun bindShareResponseToEntity(impl: ShareResponseToEntityImpl): ShareResponseToEntity

    @Binds
    abstract fun bindReencryptShareEntityContentsImpl(
        impl: ReencryptShareEntityContentsImpl
    ): ReencryptShareEntityContents

    @Binds
    abstract fun bindReencryptShareContents(
        impl: ReencryptShareContentsImpl
    ): ReencryptShareContents
}
