package proton.android.pass.featuresearchoptions.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesSearchOptionsModule {

    @Binds
    abstract fun bindSearchOptionsRepository(impl: TestSearchOptionsRepository): SearchOptionsRepository
}

