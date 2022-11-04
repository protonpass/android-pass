package me.proton.pass.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.pass.data.usecases.UpdateAutofillItem
import me.proton.pass.data.usecases.UpdateAutofillItemImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkModule {

    @Binds
    abstract fun bindAddPackageToItem(impl: UpdateAutofillItemImpl): UpdateAutofillItem
}

