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

sealed interface PlanType {

    fun humanReadableName(): String
    fun internalName(): String

    data class Free(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    data class Unknown(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    data class Trial(
        val internal: String,
        val humanReadable: String,
        val remainingDays: Int
    ) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    data class Paid(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    companion object {
        const val PLAN_NAME_FREE = "free"
        const val PLAN_NAME_PLUS = "plus"
    }
}
