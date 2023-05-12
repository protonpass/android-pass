package proton.android.pass.account.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.account.api.AccountOrchestrators

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {
    @Binds
    abstract fun bindAccountOrchestrators(impl: AccountOrchestratorsImpl): AccountOrchestrators
}
