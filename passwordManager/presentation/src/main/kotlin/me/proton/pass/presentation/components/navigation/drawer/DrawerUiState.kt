/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.pass.presentation.components.navigation.drawer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import me.proton.core.user.domain.entity.User

enum class NavigationDrawerSection { Items, Settings, Trash, Help }

@Immutable
data class DrawerUiState(
    @StringRes val appNameResId: Int,
    val appVersion: String,
    val closeOnBackEnabled: Boolean = true,
    val closeOnActionEnabled: Boolean = true,
    val currentUser: User? = null,
    val selectedSection: NavigationDrawerSection? = null
)
