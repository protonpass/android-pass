package proton.android.pass.commonui.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import proton.android.pass.commonui.api.SavedStateHandleProvider

@Module
@InstallIn(ViewModelComponent::class)
abstract class CommonUiModule {

    @Binds
    abstract fun bindSavedStateHandleProvider(
        impl: SavedStateHandleProviderImpl
    ): SavedStateHandleProvider

}
