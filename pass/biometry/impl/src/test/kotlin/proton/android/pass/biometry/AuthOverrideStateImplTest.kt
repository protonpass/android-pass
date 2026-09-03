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

package proton.android.pass.biometry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private class FakeElapsedTimeProvider(var currentMillis: Long = 0L) : ElapsedTimeProvider {
    override fun getElapsedTime(): Long = currentMillis
}

class AuthOverrideStateImplTest {

    private val elapsedTimeProvider = FakeElapsedTimeProvider()
    private val authOverrideState = AuthOverrideStateImpl(elapsedTimeProvider)

    @Test
    fun `override is inactive by default`() {
        assertThat(authOverrideState.consumeAuthOverride()).isFalse()
    }

    @Test
    fun `override is active immediately after being armed`() {
        authOverrideState.setAuthOverride(enabled = true)

        assertThat(authOverrideState.consumeAuthOverride()).isTrue()
    }

    @Test
    fun `override stays active before the TTL elapses`() {
        authOverrideState.setAuthOverride(enabled = true)

        elapsedTimeProvider.currentMillis += 59_000L

        assertThat(authOverrideState.consumeAuthOverride()).isTrue()
    }

    @Test
    fun `override expires once the TTL elapses even though it was never explicitly cleared`() {
        authOverrideState.setAuthOverride(enabled = true)

        elapsedTimeProvider.currentMillis += 60_001L

        assertThat(authOverrideState.consumeAuthOverride()).isFalse()
    }

    @Test
    fun `override becomes inactive once explicitly disabled`() {
        authOverrideState.setAuthOverride(enabled = true)
        authOverrideState.setAuthOverride(enabled = false)

        assertThat(authOverrideState.consumeAuthOverride()).isFalse()
    }

    @Test
    fun `override is single-use, a second consume within the TTL returns false`() {
        authOverrideState.setAuthOverride(enabled = true)

        assertThat(authOverrideState.consumeAuthOverride()).isTrue()
        assertThat(authOverrideState.consumeAuthOverride()).isFalse()
    }

}
