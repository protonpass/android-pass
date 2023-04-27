package proton.android.pass.image.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.image.api.ClearIconCache

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesImageModule {

    @Binds
    abstract fun bindClearIconCache(impl: TestClearIconCache): ClearIconCache
}

