package proton.android.featuresearchoptions.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchOptionsModule {

    @Binds
    abstract fun bindSearchOptionsRepository(impl: SearchOptionsRepositoryImpl): SearchOptionsRepository
}

