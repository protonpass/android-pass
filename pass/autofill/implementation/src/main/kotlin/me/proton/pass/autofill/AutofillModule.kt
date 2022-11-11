package me.proton.pass.autofill

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.autofill.api.AutofillManager

@Module
@InstallIn(SingletonComponent::class)
abstract class AutofillModule {

    @Binds
    abstract fun bindAutofillManager(impl: AutofillManagerImpl): AutofillManager
}

