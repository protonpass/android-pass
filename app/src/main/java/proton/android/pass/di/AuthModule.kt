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

/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultHelpOptionHandler
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.auth.presentation.ui.LoginActivity
import me.proton.core.user.domain.UserManager
import proton.android.pass.PassActivityOrchestrator
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordListener
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    @Suppress("LongParameterList")
    fun provideUserCheck(
        @ApplicationContext context: Context,
        accountManager: AccountManager,
        userManager: UserManager,
        sessionManager: SessionManager,
        authWithExtraPasswordListener: AuthWithExtraPasswordListener,
        passActivityOrchestrator: PassActivityOrchestrator
    ): PostLoginAccountSetup.UserCheck = PassScopeUserCheck(
        context = context,
        accountManager = accountManager,
        userManager = userManager,
        sessionManager = sessionManager,
        authWithExtraPasswordListener = authWithExtraPasswordListener,
        passActivityOrchestrator = passActivityOrchestrator
    )

    @Provides
    @Singleton
    fun providePassActivityOrchestrator(): PassActivityOrchestrator = PassActivityOrchestrator()

    @Provides
    @Singleton
    fun provideLoginBlockingHelp(): LoginActivity.BlockingHelp? = null

    @Provides
    @Singleton
    fun provideHelpOptionHandler(): HelpOptionHandler = DefaultHelpOptionHandler()
}
