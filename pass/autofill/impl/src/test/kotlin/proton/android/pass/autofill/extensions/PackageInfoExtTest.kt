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

package proton.android.pass.autofill.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

class PackageInfoExtTest {

    @Test
    fun `non-browser package name is NOT_A_BROWSER regardless of hashes`() {
        val info = PackageInfo(PackageName("com.example.app"), AppName("App"), hashes = setOf("AA"))
        assertThat(info.verifyBrowser(allowedFingerprints = setOf("AA"))).isEqualTo(BrowserVerification.NOT_A_BROWSER)
    }

    @Test
    fun `browser package name with matching fingerprint is VERIFIED`() {
        val info = PackageInfo(PackageName("com.android.chrome"), AppName("Chrome"), hashes = setOf("AA", "BB"))
        assertThat(info.verifyBrowser(allowedFingerprints = setOf("BB"))).isEqualTo(BrowserVerification.VERIFIED)
    }

    @Test
    fun `browser package name with non-matching fingerprint is UNVERIFIED`() {
        val info = PackageInfo(PackageName("com.android.chrome"), AppName("Chrome"), hashes = setOf("AA"))
        assertThat(info.verifyBrowser(allowedFingerprints = setOf("BB"))).isEqualTo(BrowserVerification.UNVERIFIED)
    }

    @Test
    fun `browser package name with no allowlist entry is UNVERIFIED`() {
        val info = PackageInfo(PackageName("mark.via"), AppName("Via"), hashes = setOf("AA"))
        assertThat(info.verifyBrowser(allowedFingerprints = emptySet())).isEqualTo(BrowserVerification.UNVERIFIED)
    }
}
