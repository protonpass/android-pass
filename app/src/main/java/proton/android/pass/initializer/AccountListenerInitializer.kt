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

package proton.android.pass.initializer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountRemoved
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.usecases.ResetAppToDefaults

class AccountListenerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint: AccountListenerInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AccountListenerInitializerEntryPoint::class.java
            )

        val lifecycleProvider = entryPoint.passAppLifecycleProvider()
        val accountManager = entryPoint.accountManager()
        val autofillManager = entryPoint.autofillManager()
        val resetAppToDefaults = entryPoint.resetAppToDefaults()

        accountManager.observe(
            lifecycle = lifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled {
            accountManager.removeAccount(it.userId)
        }.onAccountRemoved {
            resetAppToDefaults()
            autofillManager.disableAutofill()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountListenerInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun autofillManager(): AutofillManager
        fun resetAppToDefaults(): ResetAppToDefaults
    }
}
