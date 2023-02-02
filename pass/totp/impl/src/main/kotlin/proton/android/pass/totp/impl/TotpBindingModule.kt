package proton.android.pass.totp.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.totp.api.GetTotpCodeFromUri

@Module
@InstallIn(SingletonComponent::class)
abstract class TotpBindingModule {

    @Binds
    abstract fun bindGetTotpCodeFromUri(impl: GetTotpCodeFromUriImpl): GetTotpCodeFromUri
}
