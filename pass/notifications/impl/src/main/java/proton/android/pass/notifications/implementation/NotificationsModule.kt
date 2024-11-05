/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.notifications.implementation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    abstract fun bindSnackbarDispatcher(impl: SnackbarDispatcherImpl): SnackbarDispatcher

    @Binds
    abstract fun bindNotificationManager(impl: NotificationManagerImpl): NotificationManager

    @Binds
    abstract fun bindToastManager(impl: ToastManagerImpl): ToastManager
}
