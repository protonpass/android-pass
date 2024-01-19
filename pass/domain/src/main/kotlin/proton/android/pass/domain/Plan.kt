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

package proton.android.pass.domain

data class Plan(
    val planType: PlanType,
    val hideUpgrade: Boolean,
    val vaultLimit: PlanLimit,
    val aliasLimit: PlanLimit,
    val totpLimit: PlanLimit,
    val updatedAt: Long
)

sealed interface PlanLimit {

    fun limitOrNull(): Int?

    object Unlimited : PlanLimit {
        override fun limitOrNull() = null
    }

    data class Limited(val limit: Int) : PlanLimit {
        override fun limitOrNull() = limit
    }

}

sealed class PlanType(
    val internalName: String,
    val humanReadableName: String,
) {

    data class Free(
        private val name: String,
        private val displayName: String,
    ) : PlanType(
        internalName = name,
        humanReadableName = displayName,
    )

    data class Unknown(
        private val name: String,
        private val displayName: String,
    ) : PlanType(
        internalName = name,
        humanReadableName = displayName,
    )

    data class Trial(
        private val name: String,
        private val displayName: String,
        val remainingDays: Int
    ) : PlanType(
        internalName = name,
        humanReadableName = displayName,
    )

    sealed class Paid(
        internalName: String,
        humanReadableName: String,
    ) : PlanType(
        internalName = internalName,
        humanReadableName = humanReadableName,
    ) {

        data class Business(
            private val name: String,
            private val displayName: String,
        ) : Paid(
            internalName = name,
            humanReadableName = displayName,
        )

        data class Plus(
            private val name: String,
            private val displayName: String,
        ) : Paid(
            internalName = name,
            humanReadableName = displayName,
        )

    }

    companion object {
        const val PLAN_NAME_BUSINESS = "business"
        const val PLAN_NAME_FREE = "free"
        const val PLAN_NAME_PLUS = "plus"
    }

}
