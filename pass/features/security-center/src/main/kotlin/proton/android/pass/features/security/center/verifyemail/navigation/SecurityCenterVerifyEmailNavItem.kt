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

package proton.android.pass.features.security.center.verifyemail.navigation

import androidx.navigation.NavType
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem

object BreachEmailIdArgId : NavArgId {
    override val key: String = "breach_email_id"
    override val navType: NavType<*> = NavType.StringType
}

object EmailArgId : NavArgId {
    override val key: String = "email"
    override val navType: NavType<*> = NavType.StringType
}

object SecurityCenterVerifyEmailNavItem : NavItem(
    baseRoute = "security/center/verifycustomemail",
    navArgIds = listOf(BreachEmailIdArgId, EmailArgId)
) {

    fun createNavRoute(id: BreachCustomEmailId, email: String) = "$baseRoute/${id.id}/$email"
}

