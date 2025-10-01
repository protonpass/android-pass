/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.upsell.v1.navigation

import androidx.navigation.NavType
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.navigation.api.NavArgId

private const val UPSELL_NAV_ARG_KEY = "upsellKey"

internal object UpsellNavArgId : NavArgId {

    override val key: String = UPSELL_NAV_ARG_KEY

    override val navType: NavType<*> = NavType.EnumType(PaidFeature::class.java)

}
