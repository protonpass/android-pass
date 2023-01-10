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
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.i
import proton.android.pass.tracing.impl.SentryInitializer
import me.proton.core.usersettings.domain.DeviceSettingsHandler
import me.proton.core.usersettings.domain.onDeviceSettingsChanged
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.Logger
import timber.log.Timber

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            LoggerInitializerEntryPoint::class.java
        )

        // Temporarily add always DebugTree to gather logs from alpha
        val tree = Timber.DebugTree()
        val handler = entryPoint.handler()
        handler.onDeviceSettingsChanged {
            if (it.isCrashReportEnabled) {
                tree.let { tree -> Timber.plant(tree) }
            } else {
                Timber.uprootAll()
            }
        }

        PassLogger.deviceInfo()

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
    }
}


private fun Logger.deviceInfo() {
    i("-----------------------------------------")
    i("OS:          Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    i("DEVICE:      ${Build.MANUFACTURER} ${Build.MODEL}")
    i("FINGERPRINT: ${Build.FINGERPRINT}")
    i("ABI:         ${Build.SUPPORTED_ABIS.joinToString(",")}")
    i("LOCALE:      ${LocaleList.getDefault().toLanguageTags()}")
    i("-----------------------------------------")
}
