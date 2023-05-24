package proton.android.pass.state.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.state.api.SavedStateInterface

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesStateModule {

    @Binds
    abstract fun bindSavedStateInterface(impl: TestSavedStateInterface): SavedStateInterface

}
