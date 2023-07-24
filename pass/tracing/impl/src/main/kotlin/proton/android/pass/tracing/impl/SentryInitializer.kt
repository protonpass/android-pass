/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.tracing.impl

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import me.proton.core.usersettings.domain.DeviceSettingsHandler
import me.proton.core.usersettings.domain.onDeviceSettingsChanged
import me.proton.core.util.android.sentry.TimberLoggerIntegration
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.toValue

class SentryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SentryInitializerEntryPoint::class.java
        )
        val appConfig = entryPoint.appConfig()

        entryPoint.deviceSettingsHandler()
            .onDeviceSettingsChanged { settings ->
                // By default true, but should always minimal sentry logs.
                if (settings.isCrashReportEnabled) {
                    if (appConfig.sentryDSN?.isNotBlank() == true) {
                        SentryAndroid.init(context) { options ->
                            options.isDebug = appConfig.isDebug
                            options.dsn = appConfig.sentryDSN
                            options.release = appConfig.versionName
                            options.environment = appConfig.flavor.toValue()
                            if (!appConfig.isDebug) {
                                options.addIntegration(
                                    TimberLoggerIntegration(
                                        minEventLevel = SentryLevel.ERROR,
                                        minBreadcrumbLevel = SentryLevel.INFO
                                    )
                                )
                            }
                        }
                    }
                } else {
                    SentryAndroid.init(context) { options ->
                        options.dsn = ""
                    }
                }
            }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SentryInitializerEntryPoint {
        fun appConfig(): AppConfig
        fun deviceSettingsHandler(): DeviceSettingsHandler
    }
}
