/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.sync

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class SyncBackoff(private val maxDelay: Duration = DEFAULT_MAX_DELAY) {

    private var consecutiveFailures = 0

    val hasFailed: Boolean get() = consecutiveFailures > 0

    fun onSuccess() {
        consecutiveFailures = 0
    }

    fun onFailure() {
        consecutiveFailures = (consecutiveFailures + 1).coerceAtMost(MAX_EXPONENT)
    }

    fun nextDelay(baseInterval: Duration): Duration =
        (baseInterval * (1 shl consecutiveFailures)).coerceAtMost(maxDelay.coerceAtLeast(baseInterval))

    private companion object {

        private const val MAX_EXPONENT = 10
        private val DEFAULT_MAX_DELAY = 5.minutes

    }

}
