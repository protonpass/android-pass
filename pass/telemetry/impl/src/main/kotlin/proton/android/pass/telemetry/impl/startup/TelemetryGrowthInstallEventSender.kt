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

package proton.android.pass.telemetry.impl.startup

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.telemetry.api.GooglePlayInstallReferrer
import proton.android.pass.telemetry.api.InstallReferrerProvider
import proton.android.pass.telemetry.api.MetaInstallReferrer
import proton.android.pass.telemetry.api.MetaInstallReferrerProvider
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

class TelemetryGrowthInstallEventSender @Inject constructor(
    private val accountManager: AccountManager,
    private val deviceSettingsRepository: DeviceSettingsRepository,
    private val isTelemetryEnabled: IsTelemetryEnabled,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val installReferrerProvider: InstallReferrerProvider,
    private val metaInstallReferrerProvider: MetaInstallReferrerProvider,
    private val telemetryManager: TelemetryManager,
    private val clock: Clock
) {

    suspend fun start() {
        combine(
            accountManager.getPrimaryUserId(),
            deviceSettingsRepository.observeDeviceSettings().map { it.isTelemetryEnabled }
        ) { userId, isDeviceTelemetryEnabled -> userId to isDeviceTelemetryEnabled }
            .distinctUntilChanged()
            .collect { (userId, isDeviceTelemetryEnabled) ->
                if (userId != null && isDeviceTelemetryEnabled) {
                    sendIfNeeded(userId)
                }
            }
    }

    private suspend fun sendIfNeeded(userId: UserId) {
        safeRunCatching {
            val alreadySent = internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()
            if (alreadySent || !isUserTelemetryEnabled(userId)) return@safeRunCatching

            val googleReferrer = installReferrerProvider.getInstallReferrer()
            val metaReferrer = metaInstallReferrerProvider.getMetaInstallReferrer()
            PassLogger.i(
                TAG,
                "Sending TelemetryGrowth install event " +
                    "(installRef=${googleReferrer.referrer != null}, metaRef=${metaReferrer.encryptedData != null})"
            )
            telemetryManager.sendEvent(
                googleReferrer.toInstallEvent(
                    metaReferrer = metaReferrer,
                    currentTimeMs = clock.now().toEpochMilliseconds()
                )
            )
            internalSettingsRepository.setTelemetryGrowthInstallEventSent(true)
        }.onFailure {
            PassLogger.w(TAG, "Error sending TelemetryGrowth install event")
            PassLogger.w(TAG, it)
        }
    }

    private suspend fun isUserTelemetryEnabled(userId: UserId): Boolean = safeRunCatching {
        isTelemetryEnabled(userId)
    }.getOrElse {
        PassLogger.w(TAG, "Error checking telemetry enabled")
        PassLogger.w(TAG, it)
        false
    }

    private companion object {
        private const val TAG = "TelemetryGrowthInstallEventSender"
    }
}

internal fun GooglePlayInstallReferrer.toInstallEvent(
    metaReferrer: MetaInstallReferrer,
    currentTimeMs: Long
): TelemetryGrowthInstallEvent {
    return if (winsAgainst(metaReferrer)) {
        TelemetryGrowthInstallEvent(
            installRef = referrer,
            googleClickTimestampSeconds = clickTimestampSeconds,
            googleInstallBeginTimestampSeconds = installBeginTimestampSeconds,
            googleCurrentDeviceTimeMs = currentTimeMs.takeIf { hasData() },
            metaEncryptedInstallReferrer = null,
            metaAttributionTimestampSeconds = null,
            metaIsClickThrough = null
        )
    } else {
        TelemetryGrowthInstallEvent(
            installRef = referrer,
            googleClickTimestampSeconds = null,
            googleInstallBeginTimestampSeconds = null,
            googleCurrentDeviceTimeMs = null,
            metaEncryptedInstallReferrer = metaReferrer.encryptedData,
            metaAttributionTimestampSeconds = metaReferrer.attributionTimestampSeconds,
            metaIsClickThrough = metaReferrer.isClickThrough
        )
    }
}

private fun GooglePlayInstallReferrer.hasData(): Boolean =
    referrer != null || clickTimestampSeconds != null || installBeginTimestampSeconds != null

private fun GooglePlayInstallReferrer.winsAgainst(metaReferrer: MetaInstallReferrer): Boolean {
    val metaHasData = metaReferrer.encryptedData != null
    if (!metaHasData) return true
    if (!hasData()) return false
    return !isMetaReferrer() && !isOrganicReferrer()
}

private fun GooglePlayInstallReferrer.isMetaReferrer(): Boolean = hasUtmValue("utm_source", META_UTM_SOURCES)

private fun GooglePlayInstallReferrer.isOrganicReferrer(): Boolean {
    val queryString = referrer?.substringAfterLast('?') ?: return false
    return queryString.split('&').any { it == "utm_medium=organic" || it == "utm_source=google-play" }
}

private fun GooglePlayInstallReferrer.hasUtmValue(key: String, expectedValues: Set<String>): Boolean {
    val queryString = referrer?.substringAfterLast('?') ?: return false
    return queryString.split('&').any { parameter ->
        parameter.startsWith("$key=") && parameter.substringAfter('=', "") in expectedValues
    }
}

private val META_UTM_SOURCES = setOf("apps.facebook.com", "apps.instagram.com")
