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

package proton.android.pass.test.domain.plans

import proton.android.pass.domain.PlanType

object PlanTypeTestFactory {

    private val planTypes = setOf(
        Free.create(),
        Paid.Plus.create(),
        Paid.Business.create()
    )

    fun random(): PlanType = planTypes.shuffled().first()

    object Free {

        fun create(name: String = "", displayName: String = ""): PlanType.Free = PlanType.Free(name, displayName)

    }

    object Paid {

        private val paidPlanTypes = setOf(
            Plus.create(),
            Business.create()
        )

        fun random(): PlanType.Paid = paidPlanTypes.shuffled().first()


        object Plus {

            fun create(name: String = "", displayName: String = ""): PlanType.Paid =
                PlanType.Paid.Plus(name, displayName)

        }

        object Business {

            fun create(name: String = "", displayName: String = ""): PlanType.Paid =
                PlanType.Paid.Plus(name, displayName)

        }

    }

}
