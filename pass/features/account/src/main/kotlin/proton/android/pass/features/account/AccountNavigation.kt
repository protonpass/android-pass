/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.account

import me.proton.core.domain.entity.UserId

sealed interface AccountNavigation {
    data object Subscription : AccountNavigation
    data object Upgrade : AccountNavigation
    data object CloseScreen : AccountNavigation
    data object PasswordManagement : AccountNavigation
    data object RecoveryEmail : AccountNavigation
    data object SecurityKeys : AccountNavigation
    data object SetExtraPassword : AccountNavigation

    @JvmInline
    value class SignOut(val userId: UserId) : AccountNavigation

    @JvmInline
    value class ExtraPasswordOptions(val userId: UserId) : AccountNavigation
}
