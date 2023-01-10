package proton.android.pass.notifications.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.notifications.api.SnackbarMessageRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesNotificationsModule {

    @Binds
    abstract fun bindSnackbarMessageRepository(impl: TestSnackbarMessageRepository): SnackbarMessageRepository
}
