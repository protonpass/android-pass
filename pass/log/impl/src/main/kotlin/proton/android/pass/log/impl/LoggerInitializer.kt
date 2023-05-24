/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.log.impl

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.usersettings.domain.DeviceSettingsHandler
import me.proton.core.usersettings.domain.onDeviceSettingsChanged
import me.proton.core.util.kotlin.CoreLogger
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.PassLogger
import proton.android.pass.tracing.impl.SentryInitializer
import timber.log.Timber

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            LoggerInitializerEntryPoint::class.java
        )

        entryPoint.handler()
            .onDeviceSettingsChanged {
                Timber.uprootAll()
                if (it.isCrashReportEnabled) {
                    if (entryPoint.appConfig().isDebug) {
                        Timber.plant(Timber.DebugTree())
                    }
                    Timber.plant(FileLoggingTree(context))
                    deviceInfo(entryPoint.appConfig())
                }
            }

        // Forward Core Logs to Timber, using AppLogger.
        CoreLogger.set(PassLogger)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        SentryInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LoggerInitializerEntryPoint {
        fun handler(): DeviceSettingsHandler
        fun appConfig(): AppConfig
    }
}

private fun deviceInfo(appConfig: AppConfig) {
    PassLogger.i(TAG, "-----------------------------------------")
    PassLogger.i(
        TAG,
        "OS:          Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    )
    PassLogger.i(TAG, "VERSION:     ${appConfig.versionName}")
    PassLogger.i(TAG, "DEVICE:      ${Build.MANUFACTURER} ${Build.MODEL}")
    PassLogger.i(TAG, "FINGERPRINT: ${Build.FINGERPRINT}")
    PassLogger.i(TAG, "ABI:         ${Build.SUPPORTED_ABIS.joinToString(",")}")
    PassLogger.i(TAG, "LOCALE:      ${LocaleList.getDefault().toLanguageTags()}")
    PassLogger.i(TAG, "-----------------------------------------")
}

private const val TAG = "DEVICE_INFO"
