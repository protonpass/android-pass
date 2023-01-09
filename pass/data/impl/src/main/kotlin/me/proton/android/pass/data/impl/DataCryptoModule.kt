package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.impl.crypto.ReencryptShareEntityContents
import me.proton.android.pass.data.impl.crypto.ReencryptShareEntityContentsImpl
import me.proton.android.pass.data.impl.crypto.ShareEntityToShare
import me.proton.android.pass.data.impl.crypto.ShareEntityToShareImpl
import me.proton.android.pass.data.impl.crypto.ShareResponseToEntity
import me.proton.android.pass.data.impl.crypto.ShareResponseToEntityImpl

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

}
