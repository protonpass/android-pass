package proton.android.pass.notifications.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesNotificationsModule {

    @Binds
    abstract fun bindSnackbarDispatcher(impl: TestSnackbarDispatcher): SnackbarDispatcher

    @Binds
    abstract fun bindNotificationManager(impl: TestNotificationManager): NotificationManager

    @Binds
    abstract fun bindToastManager(impl: TestToastManager): ToastManager
}
