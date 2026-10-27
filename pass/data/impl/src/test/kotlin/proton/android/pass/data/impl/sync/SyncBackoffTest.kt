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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SyncBackoffTest {

    @Test
    fun `uses the base interval when nothing has failed`() {
        assertThat(SyncBackoff().nextDelay(BASE)).isEqualTo(BASE)
    }

    @Test
    fun `doubles the delay on each consecutive failure`() {
        val backoff = SyncBackoff(maxDelay = 10.minutes)

        val delays = List(3) {
            backoff.onFailure()
            backoff.nextDelay(BASE)
        }

        assertThat(delays).containsExactly(60.seconds, 120.seconds, 240.seconds).inOrder()
    }

    @Test
    fun `never exceeds the max delay`() {
        val backoff = SyncBackoff(maxDelay = 5.minutes)

        repeat(50) { backoff.onFailure() }

        assertThat(backoff.nextDelay(BASE)).isEqualTo(5.minutes)
    }

    @Test
    fun `returns to the base interval after a success`() {
        val backoff = SyncBackoff()
        repeat(3) { backoff.onFailure() }

        backoff.onSuccess()

        assertThat(backoff.nextDelay(BASE)).isEqualTo(BASE)
    }

    @Test
    fun `reports failure only while consecutive failures are pending`() {
        val backoff = SyncBackoff()
        assertThat(backoff.hasFailed).isFalse()

        backoff.onFailure()
        assertThat(backoff.hasFailed).isTrue()

        backoff.onSuccess()
        assertThat(backoff.hasFailed).isFalse()
    }

    private companion object {
        private val BASE = 30.seconds
    }
}
