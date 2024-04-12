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

package proton.android.pass.features.security.center.report.navigation

import androidx.navigation.NavType
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.features.security.center.shared.navigation.BreachEmailIdArgId
import proton.android.pass.features.security.center.shared.navigation.EmailArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder

object BreachesArgId : NavArgId {
    override val key: String = "breaches"
    override val navType: NavType<*> = NavType.IntType
}

object EmailTypeArgId : NavArgId {
    override val key: String = "email_type"
    override val navType: NavType<*> = NavType.EnumType(EmailType::class.java)
}

enum class EmailType {
    Custom,
    Alias,
    Proton
}

object SecurityCenterReportNavItem : NavItem(
    baseRoute = "security/center/report",
    navArgIds = listOf(EmailTypeArgId, BreachEmailIdArgId, EmailArgId, BreachesArgId)
) {
    fun createNavRoute(
        emailType: EmailType,
        id: BreachCustomEmailId,
        email: String,
        breaches: Int
    ): String = "$baseRoute/$emailType/${id.id}/${NavParamEncoder.encode(email)}/$breaches"
}

