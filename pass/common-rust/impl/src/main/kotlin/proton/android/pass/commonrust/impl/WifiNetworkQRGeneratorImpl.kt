/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.WifiSecurity
import proton.android.pass.commonrust.api.WifiNetworkQRGenerator
import proton.android.pass.domain.WifiSecurityType
import javax.inject.Inject
import javax.inject.Singleton
import proton.android.pass.commonrust.WifiQrCodeGenerator as RustWifiQrCodeGenerator

@Singleton
class WifiNetworkQRGeneratorImpl @Inject constructor() : WifiNetworkQRGenerator {

    private val generator: RustWifiQrCodeGenerator by lazy { RustWifiQrCodeGenerator() }

    override fun generateQr(
        ssid: String,
        password: String,
        wifiSecurity: WifiSecurityType
    ): Result<String> = runCatching {
        generator.generateSvgQrCode(
            ssid = ssid,
            password = password,
            security = when (wifiSecurity) {
                WifiSecurityType.Unknown -> WifiSecurity.UNSPECIFIED
                WifiSecurityType.WPA -> WifiSecurity.WPA
                WifiSecurityType.WPA2 -> WifiSecurity.WPA2
                WifiSecurityType.WPA3 -> WifiSecurity.WPA3
                WifiSecurityType.WEP -> WifiSecurity.WEP
            }
        )
    }
}
