package me.proton.pass.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.pass.data.usecases.AddPackageNameToItem
import me.proton.pass.data.usecases.AddPackageNameToItemImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkModule {

    @Binds
    abstract fun bindAddPackageToItem(impl: AddPackageNameToItemImpl): AddPackageNameToItem
}

