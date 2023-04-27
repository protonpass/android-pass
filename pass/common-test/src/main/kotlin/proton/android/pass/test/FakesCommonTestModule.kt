package proton.android.pass.test

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.payment.domain.PaymentManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesCommonTestModule {

    @Binds
    abstract fun bindAccountManager(
        impl: TestAccountManager
    ): AccountManager

    @Binds
    abstract fun bindPaymentManager(
        impl: TestPaymentManager
    ): PaymentManager
}
