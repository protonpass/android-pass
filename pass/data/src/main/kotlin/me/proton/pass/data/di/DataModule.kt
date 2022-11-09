package me.proton.pass.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.pass.data.autofill.AutofillManagerImpl
import me.proton.pass.data.usecases.UpdateAutofillItem
import me.proton.pass.data.usecases.UpdateAutofillItemImpl
import me.proton.pass.domain.autofill.AutofillManager

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAddPackageToItem(impl: UpdateAutofillItemImpl): UpdateAutofillItem

    @Binds
    abstract fun bindAutofillManager(impl: AutofillManagerImpl): AutofillManager
}

