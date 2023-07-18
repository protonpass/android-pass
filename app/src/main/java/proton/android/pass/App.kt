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

package proton.android.pass

import android.app.Application
import androidx.lifecycle.Lifecycle
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountRemoved
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.usecases.ClearAppData
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject
    lateinit var preferenceRepository: UserPreferencesRepository

    @Inject
    lateinit var accountManager: Provider<AccountManager>

    @Inject
    lateinit var clearAppData: ClearAppData

    @Inject
    lateinit var passAppLifecycleProvider: PassAppLifecycleProvider

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        MainInitializer.init(this)
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)

        registerAccountManagerCallbacks()
    }

    private fun registerAccountManagerCallbacks() {
        accountManager.get().observe(
            lifecycle = passAppLifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled {
            accountManager.get().removeAccount(it.userId)
        }.onAccountRemoved {
            clearAppData.invoke()
        }
    }
}
