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

package proton.android.pass.ui

import me.proton.core.domain.entity.UserId

sealed interface AppNavigation {
    data object SignOut : AppNavigation
    data object ForceSignOut : AppNavigation
    data object Report : AppNavigation
    data object Subscription : AppNavigation
    data object Upgrade : AppNavigation
    data object Finish : AppNavigation
    data object Restart : AppNavigation
    data object PasswordManagement : AppNavigation
    data object RecoveryEmail : AppNavigation
    data object AddAccount : AppNavigation

    @JvmInline
    value class SwitchAccount(val userId: UserId) : AppNavigation
}
