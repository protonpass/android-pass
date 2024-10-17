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

package proton.android.pass.composecomponents.impl.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.domain.time.RemainingTime

@Composable
fun passRemainingTimeText(remainingTime: RemainingTime): String? = when {
    remainingTime.years > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_years,
            count = remainingTime.years,
            remainingTime.years
        )
    }

    remainingTime.months > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_months,
            count = remainingTime.months,
            remainingTime.months
        )
    }

    remainingTime.weeks > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_weeks,
            count = remainingTime.weeks,
            remainingTime.weeks
        )
    }

    remainingTime.days > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_days,
            count = remainingTime.days,
            remainingTime.days
        )
    }

    remainingTime.hours > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_hours,
            count = remainingTime.hours,
            remainingTime.hours
        )
    }

    remainingTime.minutes > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_minutes,
            count = remainingTime.minutes,
            remainingTime.minutes
        )
    }

    remainingTime.seconds > 0 -> {
        pluralStringResource(
            id = R.plurals.time_unit_seconds,
            count = remainingTime.seconds,
            remainingTime.seconds
        )
    }

    else -> {
        null
    }
}
