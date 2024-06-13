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

package proton.android.pass.domain.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class RemainingTime(
    private val endInstant: Instant,
    private val startInstant: Instant = Clock.System.now()
) {

    private val remainingDuration: Duration by lazy {
        endInstant - startInstant
    }

    val days: Int by lazy {
        remainingDuration.inWholeDays.toInt()
    }

    val hours: Int by lazy {
        (remainingDuration - days.days).inWholeHours.toInt()
    }

    val minutes: Int by lazy {
        (remainingDuration - days.days - hours.hours).inWholeMinutes.toInt()
    }

    val seconds: Int by lazy {
        (remainingDuration - days.days - hours.hours - minutes.minutes).inWholeSeconds.toInt()
    }

}
