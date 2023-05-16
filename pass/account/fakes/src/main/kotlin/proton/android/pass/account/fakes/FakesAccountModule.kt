package proton.android.pass.account.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.account.api.AccountOrchestrators

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesAccountModule {

    @Binds
    abstract fun bindAccountOrchestrators(
        impl: TestAccountOrchestrators
    ): AccountOrchestrators

    @Binds
    abstract fun bindAccountManager(
        impl: TestAccountManager
    ): AccountManager

    @Binds
    abstract fun bindPaymentManager(
        impl: TestPaymentManager
    ): PaymentManager
}
