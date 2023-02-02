package proton.android.pass.totp.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.android.pass.totp.api.TotpManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesTotpModule {

    @Binds
    abstract fun bindTotpManager(impl: TestTotpManager): TotpManager

    @Binds
    abstract fun bindGetCodeFromUri(impl: TestGetTotpCodeFromUri): GetTotpCodeFromUri
}
