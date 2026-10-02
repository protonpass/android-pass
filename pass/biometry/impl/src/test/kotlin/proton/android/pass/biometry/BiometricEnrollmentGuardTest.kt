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

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.ProviderException

class BiometricEnrollmentGuardTest {

    private lateinit var guardKey: TestEnrollmentGuardKey
    private lateinit var guard: BiometricEnrollmentGuard

    @Before
    fun setup() {
        guardKey = TestEnrollmentGuardKey()
        guard = BiometricEnrollmentGuard(guardKey)
    }

    @Test
    fun `valid key reports enrollment unchanged`() {
        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guardKey.deleteCount).isEqualTo(0)
    }

    @Test
    fun `no biometric enrolled reports enrollment unchanged without retry or deletion`() {
        guardKey.failures += noBiometricEnrolledError()

        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guardKey.initCount).isEqualTo(1)
        assertThat(guardKey.deleteCount).isEqualTo(0)
    }

    @Test
    fun `key permanently invalidated reports enrollment changed and deletes key`() {
        guardKey.failures += KeyPermanentlyInvalidatedException()

        assertThat(guard.hasEnrollmentChanged()).isTrue()
        assertThat(guardKey.initCount).isEqualTo(1)
        assertThat(guardKey.deleteCount).isEqualTo(1)
    }

    @Test
    fun `invalid key reports enrollment changed and deletes key`() {
        guardKey.failures += InvalidKeyException()

        assertThat(guard.hasEnrollmentChanged()).isTrue()
        assertThat(guardKey.deleteCount).isEqualTo(1)
    }

    @Test
    fun `user not authenticated reports enrollment unchanged`() {
        guardKey.failures += UserNotAuthenticatedException()

        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guardKey.deleteCount).isEqualTo(0)
    }

    @Test
    fun `transient keystore failure is retried once`() {
        guardKey.failures += ProviderException("transient")

        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guardKey.initCount).isEqualTo(2)
    }

    @Test
    fun `unrelated invalid algorithm parameter keeps failing closed`() {
        val error = InvalidAlgorithmParameterException(
            "init failed",
            IllegalStateException("Secure lock screen must be enabled")
        )
        guardKey.failures += listOf(error, error)

        assertThat(guard.hasEnrollmentChanged()).isTrue()
        assertThat(guardKey.initCount).isEqualTo(2)
    }

    @Test
    fun `persistent unknown failure fails closed`() {
        guardKey.failures += listOf(ProviderException("broken"), ProviderException("broken"))

        assertThat(guard.hasEnrollmentChanged()).isTrue()
    }

    @Test
    fun `reset recreates key and arms the guard`() {
        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.Armed)
        assertThat(guardKey.deleteCount).isEqualTo(1)
        assertThat(guardKey.initCount).isEqualTo(1)
    }

    @Test
    fun `reset with user not authenticated arms the guard`() {
        guardKey.failures += UserNotAuthenticatedException()

        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.Armed)
    }

    @Test
    fun `reset with no biometric enrolled reports it without failing`() {
        guardKey.failures += noBiometricEnrolledError()

        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.NoBiometricEnrolled)
        assertThat(guardKey.initCount).isEqualTo(1)
    }

    @Test
    fun `reset with persistent unknown failure fails`() {
        guardKey.failures += listOf(ProviderException("broken"), ProviderException("broken"))

        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.Failed)
        assertThat(guardKey.deleteCount).isEqualTo(2)
    }

    private fun noBiometricEnrolledError() = InvalidAlgorithmParameterException(
        IllegalStateException(
            "At least one biometric must be enrolled to create keys requiring user authentication for every use"
        )
    )
}

private class TestEnrollmentGuardKey : EnrollmentGuardKey {

    val failures = ArrayDeque<Exception>()

    var initCount = 0
        private set

    var deleteCount = 0
        private set

    override fun initCipher() {
        initCount++
        failures.removeFirstOrNull()?.let { throw it }
    }

    override fun delete() {
        deleteCount++
    }
}
