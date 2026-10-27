/*
 * Copyright (c) 2026 Proton AG
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
package proton.android.pass.ui.navigation

import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.AuthOrigin
import proton.android.pass.features.profile.MASTER_PASSWORD_VERIFIED_KEY

internal data class NavigationResult(val key: String, val value: Boolean)

internal fun AuthNavigation.lockChangeResult(): NavigationResult? = when {
    this is AuthNavigation.Success && origin == AuthOrigin.CHANGE_APP_LOCK_TYPE ->
        NavigationResult(key = MASTER_PASSWORD_VERIFIED_KEY, value = true)

    else -> null
}
