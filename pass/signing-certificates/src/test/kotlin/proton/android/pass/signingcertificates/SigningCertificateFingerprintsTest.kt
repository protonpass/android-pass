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

package proton.android.pass.signingcertificates

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class SigningCertificateFingerprintsTest {

    @Test
    internal fun `converts each signing certificate to an uppercase colon-separated SHA-256 fingerprint`() {
        val fingerprints = SigningCertificateFingerprints.fingerprintsOf(
            listOf("certificate-a".encodeToByteArray())
        )

        assertThat(fingerprints).containsExactly(
            "BB:31:3A:B8:86:17:77:EF:AC:A8:03:9B:A2:04:89:30:57:66:97:26:1F:5D:3E:3C:A4:A7:38:BB:8A:3B:8C:8E"
        )
    }

    @Test
    internal fun `hasSingleSigner is true for exactly one signer`() {
        val result = SigningCertificateFingerprints.hasSingleSigner(
            signingCertificateBytes = listOf(byteArrayOf(1, 2, 3, 4)),
            hasMultipleSigners = false
        )

        assertThat(result).isTrue()
    }

    @Test
    internal fun `hasSingleSigner is false for an empty signer list`() {
        val result = SigningCertificateFingerprints.hasSingleSigner(
            signingCertificateBytes = emptyList(),
            hasMultipleSigners = false
        )

        assertThat(result).isFalse()
    }

    @Test
    internal fun `hasSingleSigner is false when multiple signers are reported`() {
        val result = SigningCertificateFingerprints.hasSingleSigner(
            signingCertificateBytes = listOf(byteArrayOf(1), byteArrayOf(2)),
            hasMultipleSigners = true
        )

        assertThat(result).isFalse()
    }
}
