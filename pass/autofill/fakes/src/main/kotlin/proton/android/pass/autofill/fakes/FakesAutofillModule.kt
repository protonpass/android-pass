package proton.android.pass.autofill.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.autofill.api.AutofillManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesAutofillModule {

    @Binds
    abstract fun bindAutofillManager(impl: TestAutofillManager): AutofillManager
}
