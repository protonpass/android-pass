package me.proton.android.pass.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.ui.autofill.search.AutofillListSecretsActivity
import me.proton.android.pass.ui.autofill.save.AutofillSaveSecretActivity
import me.proton.core.pass.autofill.service.di.AutofillSaveActivityClass
import me.proton.core.pass.autofill.service.di.AutofillSearchActivityClass
import kotlin.reflect.KClass

@Module
@InstallIn(SingletonComponent::class)
class AutofillModule {

    @AutofillSaveActivityClass
    @Provides
    fun provideAutofillSaveActivityClass(): KClass<*> = AutofillSaveSecretActivity::class

    @AutofillSearchActivityClass
    @Provides
    fun provideAutofillSearchActivityClass(): KClass<*> = AutofillListSecretsActivity::class

}
