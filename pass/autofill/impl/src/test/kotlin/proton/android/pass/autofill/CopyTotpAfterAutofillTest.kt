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

package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.autofill.service.R
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.totp.fakes.FakeGetTotpCodeFromUri

class CopyTotpAfterAutofillTest {

    private lateinit var getAppDispatchers: FakeAppDispatchers
    private lateinit var getTotpCodeFromUri: FakeGetTotpCodeFromUri
    private lateinit var clipboardManager: FakeClipboardManager
    private lateinit var toastManager: FakeToastManager
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var preferenceRepository: FakePreferenceRepository
    private lateinit var instance: CopyTotpAfterAutofill

    @Before
    fun setup() {
        getAppDispatchers = FakeAppDispatchers()
        getTotpCodeFromUri = FakeGetTotpCodeFromUri()
        clipboardManager = FakeClipboardManager()
        toastManager = FakeToastManager()
        telemetryManager = FakeTelemetryManager()
        preferenceRepository = FakePreferenceRepository().apply {
            setCopyTotpToClipboardEnabled(CopyTotpToClipboard.Enabled)
        }
        instance = CopyTotpAfterAutofill(
            getTotpCodeFromUri = getTotpCodeFromUri,
            clipboardManager = clipboardManager,
            toastManager = toastManager,
            telemetryManager = telemetryManager,
            preferenceRepository = preferenceRepository,
            appDispatchers = getAppDispatchers
        )
    }

    @Test
    fun `copies the generated code to the clipboard when the setting is enabled`() = runTest {
        getTotpCodeFromUri.setResult(Result.success("123456"))

        instance.run(TOTP_URI)
        advanceUntilIdle()

        assertThat(clipboardManager.getContents()).isEqualTo("123456")
        assertThat(toastManager.stringResourceMessage).isEqualTo(R.string.autofill_notification_copy_to_clipboard)
        assertThat(telemetryManager.getMemory()).contains(MFAAutofillCopied)
    }

    @Test
    fun `copies the code as secure content`() = runTest {
        getTotpCodeFromUri.setResult(Result.success("123456"))

        instance.run(TOTP_URI)
        advanceUntilIdle()

        assertThat(clipboardManager.lastCopiedWasSecure).isTrue()
    }

    @Test
    fun `does not copy anything when the setting is disabled`() = runTest {
        preferenceRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.NotEnabled)
        getTotpCodeFromUri.setResult(Result.success("123456"))

        instance.run(TOTP_URI)
        advanceUntilIdle()

        assertThat(clipboardManager.getContents()).isEmpty()
        assertThat(toastManager.stringResourceMessage).isNull()
    }

    @Test
    fun `does not copy anything when the uri is blank`() = runTest {
        getTotpCodeFromUri.setResult(Result.success("123456"))

        instance.run("")
        advanceUntilIdle()

        assertThat(clipboardManager.getContents()).isEmpty()
    }

    @Test
    fun `does not copy or toast when code generation fails`() = runTest {
        getTotpCodeFromUri.setResult(Result.failure(IllegalStateException("bad uri")))

        instance.run(TOTP_URI)
        advanceUntilIdle()

        assertThat(clipboardManager.getContents()).isEmpty()
        assertThat(toastManager.stringResourceMessage).isNull()
        assertThat(telemetryManager.getMemory()).doesNotContain(MFAAutofillCopied)
    }

    private companion object {
        const val TOTP_URI = "otpauth://totp/example.com:user?secret=JBSWY3DPEHPK3PXP&issuer=example"
    }
}
