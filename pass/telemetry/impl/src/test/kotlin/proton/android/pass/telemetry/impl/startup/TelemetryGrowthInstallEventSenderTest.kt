/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.telemetry.impl.startup

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeDeviceSettingsRepository
import proton.android.pass.account.fakes.telemetry.FakeIsTelemetryEnabled
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.telemetry.api.GooglePlayInstallReferrer
import proton.android.pass.telemetry.api.MetaInstallReferrer
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent
import proton.android.pass.telemetry.fakes.FakeInstallReferrerProvider
import proton.android.pass.telemetry.fakes.FakeMetaInstallReferrerProvider
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.FixedClock

class TelemetryGrowthInstallEventSenderTest {

    private lateinit var accountManager: FakeAccountManager
    private lateinit var deviceSettingsRepository: FakeDeviceSettingsRepository
    private lateinit var isTelemetryEnabled: FakeIsTelemetryEnabled
    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var installReferrerProvider: FakeInstallReferrerProvider
    private lateinit var metaInstallReferrerProvider: FakeMetaInstallReferrerProvider
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var instance: TelemetryGrowthInstallEventSender

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        deviceSettingsRepository = FakeDeviceSettingsRepository()
        isTelemetryEnabled = FakeIsTelemetryEnabled().apply { setEnabled(true) }
        internalSettingsRepository = FakeInternalSettingsRepository()
        installReferrerProvider = FakeInstallReferrerProvider().apply { setReferrer(GooglePlayInstallReferrer()) }
        metaInstallReferrerProvider = FakeMetaInstallReferrerProvider().apply { setReferrer(META_REFERRER) }
        telemetryManager = FakeTelemetryManager()
        instance = TelemetryGrowthInstallEventSender(
            accountManager = accountManager,
            deviceSettingsRepository = deviceSettingsRepository,
            isTelemetryEnabled = isTelemetryEnabled,
            internalSettingsRepository = internalSettingsRepository,
            installReferrerProvider = installReferrerProvider,
            metaInstallReferrerProvider = metaInstallReferrerProvider,
            telemetryManager = telemetryManager,
            clock = FixedClock(Instant.fromEpochMilliseconds(CURRENT_TIME_MS))
        )
    }

    @Test
    fun `sends install event with meta referrer when user exists and telemetry is enabled`() = runTest {
        startSender()

        assertThat(telemetryManager.getMemory()).containsExactly(
            TelemetryGrowthInstallEvent(
                metaEncryptedInstallReferrer = META_REFERRER.encryptedData,
                metaAttributionTimestampSeconds = META_REFERRER.attributionTimestampSeconds,
                metaIsClickThrough = META_REFERRER.isClickThrough
            )
        )
        assertThat(internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()).isTrue()
    }

    @Test
    fun `does not query meta nor send install event without a primary user`() = runTest {
        accountManager.sendPrimaryUserId(null)

        startSender()

        assertNothingSent()
    }

    @Test
    fun `does not query meta nor send install event when device telemetry is disabled`() = runTest {
        deviceSettingsRepository.updateIsTelemetryEnabled(false)

        startSender()

        assertNothingSent()
    }

    @Test
    fun `does not query meta nor send install event when user telemetry is disabled`() = runTest {
        isTelemetryEnabled.setEnabled(false)

        startSender()

        assertNothingSent()
    }

    @Test
    fun `does not query meta nor send install event when already sent`() = runTest {
        internalSettingsRepository.setTelemetryGrowthInstallEventSent(true)

        startSender()

        assertThat(metaInstallReferrerProvider.queryCount).isEqualTo(0)
        assertThat(telemetryManager.getMemory()).isEmpty()
    }

    @Test
    fun `sends install event once user logs in`() = runTest {
        accountManager.sendPrimaryUserId(null)
        startSender()

        accountManager.sendPrimaryUserId(UserId(USER_ID))
        runCurrent()

        assertThat(telemetryManager.getMemory()).hasSize(1)
        assertThat(metaInstallReferrerProvider.queryCount).isEqualTo(1)
    }

    @Test
    fun `sends install event once when device telemetry is enabled later`() = runTest {
        deviceSettingsRepository.updateIsTelemetryEnabled(false)
        startSender()

        deviceSettingsRepository.updateIsTelemetryEnabled(true)
        runCurrent()
        deviceSettingsRepository.updateIsTelemetryEnabled(false)
        runCurrent()
        deviceSettingsRepository.updateIsTelemetryEnabled(true)
        runCurrent()

        assertThat(telemetryManager.getMemory()).hasSize(1)
        assertThat(metaInstallReferrerProvider.queryCount).isEqualTo(1)
    }

    @Test
    fun `survives install referrer failure without marking the install event as sent`() = runTest {
        installReferrerProvider.setException(IllegalStateException("referrer failure"))

        startSender()

        assertThat(telemetryManager.getMemory()).isEmpty()
        assertThat(internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()).isFalse()

        installReferrerProvider.setException(null)
        deviceSettingsRepository.updateIsTelemetryEnabled(false)
        runCurrent()
        deviceSettingsRepository.updateIsTelemetryEnabled(true)
        runCurrent()

        assertThat(telemetryManager.getMemory()).hasSize(1)
        assertThat(internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()).isTrue()
    }

    private fun TestScope.startSender() {
        backgroundScope.launch { instance.start() }
        runCurrent()
    }

    private suspend fun assertNothingSent() {
        assertThat(metaInstallReferrerProvider.queryCount).isEqualTo(0)
        assertThat(telemetryManager.getMemory()).isEmpty()
        assertThat(internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()).isFalse()
    }

    private companion object {
        private const val USER_ID = "user-id"
        private const val CURRENT_TIME_MS = 1_700_000_000_000L
        private val META_REFERRER = MetaInstallReferrer(
            encryptedData = "encrypted",
            attributionTimestampSeconds = 1_699_999_000L,
            isClickThrough = true
        )
    }
}
