package proton.android.pass.commonui.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.commonui.api.SavedStateHandleProvider

@Module
@InstallIn(ViewModelComponent::class)
abstract class CommonUiModule {

    @Binds
    abstract fun bindSavedStateHandleProvider(
        impl: SavedStateHandleProviderImpl
    ): SavedStateHandleProvider

}


@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonCommonUiModule {

    @Binds
    abstract fun bindAppLifecycleProvider(
        impl: PassAppLifecycleObserverImpl
    ): PassAppLifecycleProvider
}
