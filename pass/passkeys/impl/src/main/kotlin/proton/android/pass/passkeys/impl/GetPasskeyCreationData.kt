/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.passkeys.impl

import android.os.Build
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.domain.PasskeyCreationData
import java.util.Locale
import javax.inject.Inject

interface GetPasskeyCreationData {
    operator fun invoke(): PasskeyCreationData
}

class GetPasskeyCreationDataImpl @Inject constructor(
    private val appConfig: AppConfig
): GetPasskeyCreationData {
    override fun invoke(): PasskeyCreationData = PasskeyCreationData(
        osName = "Android",
        osVersion = Build.VERSION.RELEASE,
        deviceName = deviceName(),
        appVersion = "android-pass@${appConfig.versionName}"
    )

    private fun deviceName(): String {
        val manufacturer = Build.BRAND.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }

        return "$manufacturer ${Build.MODEL}"
    }

}

