package proton.android.pass.commonui.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.commonui.api.SavedStateHandleProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesCommonUiModule {

    @Binds
    abstract fun bindSavedStateHandleProvider(
        impl: TestSavedStateHandleProvider
    ): SavedStateHandleProvider

}
