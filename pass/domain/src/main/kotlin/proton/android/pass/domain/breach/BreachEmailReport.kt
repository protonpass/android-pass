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

package proton.android.pass.domain.breach

import me.proton.core.user.domain.entity.AddressId
import me.proton.core.util.kotlin.hasFlag

sealed interface BreachEmailReport {

    val email: String
    val breachCount: Int
    val flags: Int
    val lastBreachTime: Int?
    val isMonitoringDisabled: Boolean
    val hasBreaches: Boolean

    data class Custom(
        val id: CustomEmailId,
        val isVerified: Boolean,
        override val email: String,
        override val breachCount: Int,
        override val flags: Int,
        override val lastBreachTime: Int?
    ) : BreachEmailReport {

        override val isMonitoringDisabled: Boolean = flags
            .hasFlag(EmailFlag.MonitoringDisabled.value)

        override val hasBreaches: Boolean = breachCount > 0
    }

    data class Proton(
        val addressId: AddressId,
        override val email: String,
        override val breachCount: Int,
        override val flags: Int,
        override val lastBreachTime: Int?
    ): BreachEmailReport {

        override val isMonitoringDisabled: Boolean = flags
            .hasFlag(EmailFlag.MonitoringDisabled.value)

        override val hasBreaches: Boolean = breachCount > 0

    }

}
