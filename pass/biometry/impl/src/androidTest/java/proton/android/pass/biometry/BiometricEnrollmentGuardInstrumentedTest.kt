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

import android.os.ParcelFileDescriptor
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class BiometricEnrollmentGuardInstrumentedTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val biometricManager = BiometricManager.from(instrumentation.targetContext)
    private var pinSetByTest = false
    private var virtualEnrollmentsChanged = false

    private lateinit var guard: BiometricEnrollmentGuard

    @Before
    fun setUp() {
        deleteGuardKey()
        guard = BiometricEnrollmentGuard(AndroidKeyStoreEnrollmentGuardKey())
    }

    @After
    fun tearDown() {
        if (virtualEnrollmentsChanged) {
            setVirtualEnrollments("")
            virtualEnrollmentsChanged = false
        }
        if (pinSetByTest) {
            shell("locksettings clear --old $PIN")
            shell("locksettings set-disabled true")
            pinSetByTest = false
        }
        deleteGuardKey()
    }

    @Test
    fun noBiometricEnrolled_reportsNotChanged() {
        setVirtualEnrollments("")
        ensureDeviceCredential()
        assumeTrue(
            "Requires no strong biometric enrolled",
            biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
        )

        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.NoBiometricEnrolled)
        assertThat(guard.hasEnrollmentChanged()).isFalse()
    }

    @Test
    fun enrollmentChange_reportsChangedAndDeletesKey() {
        armWithEnrolledFingerprint()

        setVirtualEnrollments(SECOND_FINGERPRINT_ID)

        assertThat(guard.hasEnrollmentChanged()).isTrue()
        assertThat(guardKeyExists()).isFalse()
    }

    @Test
    fun unlockWithoutEnrollmentChange_reportsNotChanged() {
        armWithEnrolledFingerprint()

        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guard.hasEnrollmentChanged()).isFalse()
        assertThat(guardKeyExists()).isTrue()
    }

    private fun armWithEnrolledFingerprint() {
        ensureDeviceCredential()
        assumeTrue(
            "$COVERAGE_TAG: enrollment-change inactive — no fingerprint shell command on this image",
            !shell("cmd fingerprint help").contains(NO_SHELL_COMMAND),
        )
        assumeTrue(
            "$COVERAGE_TAG: enrollment-change inactive — root required for virtual fingerprint enrollment",
            shell("id").contains(ROOT_UID),
        )
        setVirtualEnrollments(FIRST_FINGERPRINT_ID)
        val enrolled = waitForStrongBiometricReady()
        assumeTrue(
            "$COVERAGE_TAG: enrollment-change inactive — virtual enrollment did not register, " +
                "canAuthenticate=${biometricManager.canAuthenticate(BIOMETRIC_STRONG)}",
            enrolled,
        )

        assertThat(guard.reset()).isEqualTo(EnrollmentGuardSetupResult.Armed)
        assertThat(guardKeyExists()).isTrue()
    }

    private fun waitForStrongBiometricReady(): Boolean {
        repeat(MAX_ENROLLMENT_SYNC_ATTEMPTS) {
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) return true
            Thread.sleep(ENROLLMENT_SYNC_INTERVAL_MS)
        }
        return false
    }

    private fun setVirtualEnrollments(value: String) {
        shell("setprop $VIRTUAL_ENROLLMENTS_PROP '$value'")
        shell("cmd fingerprint sync")
        virtualEnrollmentsChanged = true
    }

    private fun ensureDeviceCredential() {
        if (!biometricManager.isDeviceSecure()) {
            shell("locksettings set-pin $PIN")
            pinSetByTest = true
        }
    }

    private fun BiometricManager.isDeviceSecure(): Boolean =
        canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS

    private fun shell(command: String): String {
        val pfd = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes().decodeToString() }
    }

    private fun guardKeyExists(): Boolean = androidKeyStore().containsAlias(GUARD_KEY_ALIAS)

    private fun deleteGuardKey() {
        androidKeyStore().deleteEntry(GUARD_KEY_ALIAS)
    }

    private fun androidKeyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private companion object {
        private const val COVERAGE_TAG = "PASS-BIOMETRY-COVERAGE"
        private const val NO_SHELL_COMMAND = "No shell command implementation"
        private const val ROOT_UID = "uid=0"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GUARD_KEY_ALIAS = "proton_pass_biometric_enrollment_guard"
        private const val PIN = "123456"
        private const val VIRTUAL_ENROLLMENTS_PROP = "persist.vendor.fingerprint.virtual.enrollments"
        private const val FIRST_FINGERPRINT_ID = "1"
        private const val SECOND_FINGERPRINT_ID = "2"
        private const val MAX_ENROLLMENT_SYNC_ATTEMPTS = 10
        private const val ENROLLMENT_SYNC_INTERVAL_MS = 500L
    }
}
