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

package proton.android.pass.preferences

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val IMMEDIATELY = 1
private const val IN_ONE_MINUTE = 3
private const val IN_TWO_MINUTES = 4
private const val IN_FIVE_MINUTES = 5
private const val IN_TEN_MINUTES = 6
private const val IN_ONE_HOUR = 7
private const val IN_FOUR_HOURS = 8

enum class AppLockTimePreference {
    Immediately,
    InOneMinute,
    InTwoMinutes,
    InFiveMinutes,
    InTenMinutes,
    InOneHour,
    InFourHours;

    fun value(): Int = when (this) {
        Immediately -> IMMEDIATELY
        InOneMinute -> IN_ONE_MINUTE
        InTwoMinutes -> IN_TWO_MINUTES
        InFiveMinutes -> IN_FIVE_MINUTES
        InTenMinutes -> IN_TEN_MINUTES
        InOneHour -> IN_ONE_HOUR
        InFourHours -> IN_FOUR_HOURS
    }

    fun toDuration(): Duration = when (this) {
        InOneMinute -> 1.minutes
        InTwoMinutes -> 2.minutes
        InFiveMinutes -> 5.minutes
        InTenMinutes -> 10.minutes
        InOneHour -> 1.hours
        InFourHours -> 4.hours
        else -> Duration.ZERO
    }

    companion object {
        fun from(value: Int): AppLockTimePreference = when (value) {
            IMMEDIATELY -> Immediately
            IN_ONE_MINUTE -> InOneMinute
            IN_TWO_MINUTES -> InTwoMinutes
            IN_FIVE_MINUTES -> InFiveMinutes
            IN_TEN_MINUTES -> InTenMinutes
            IN_ONE_HOUR -> InOneHour
            IN_FOUR_HOURS -> InFourHours
            else -> InTwoMinutes
        }
    }
}
