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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

@Suppress("MaxLineLength")
class PasskeyJsonSanitizerTest {

    @Test
    fun `can sanitize paypal json`() {
        val input = """
            {"attestation":"none","authenticatorSelection":{"residentKey":"preferred","userVerification":"preferred"},"challenge":"qEb-L-3-cp65J8-VJlZACfzVeB98j2AUY-JexPTBiqBrLyec9XozWpy3SHo84UTtEAztuUVuRCwg0aF9zaE1JA\u003d","timeout":1800000.0,"excludeCredentials":[],"extensions":{"credProps":true},"pubKeyCredParams":[{"alg":-7,"type":"public-key"},{"alg":-257,"type":"public-key"}],"rp":{"id":"paypal.com","name":"paypal.com"},"user":{"displayName":"rerere","id":"Y21WeVpYSmw","name":"rerere"}}
        """.trimIndent()

        val expected = """
            {"attestation":"none","authenticatorSelection":{"residentKey":"preferred","userVerification":"preferred"},"challenge":"qEb-L-3-cp65J8-VJlZACfzVeB98j2AUY-JexPTBiqBrLyec9XozWpy3SHo84UTtEAztuUVuRCwg0aF9zaE1JA=","timeout":1800000,"excludeCredentials":[],"extensions":{"credProps":true},"pubKeyCredParams":[{"alg":-7,"type":"public-key"},{"alg":-257,"type":"public-key"}],"rp":{"id":"paypal.com","name":"paypal.com"},"user":{"displayName":"rerere","id":"Y21WeVpYSmw","name":"rerere"}}
        """.trimIndent()

        val res = PasskeyJsonSanitizer.sanitize(input)
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `can sanitize ebay json`() {
        val input = """
            {"attestation":"direct","authenticatorSelection":{"authenticatorAttachment":"platform","requireResidentKey":true,"userVerification":"required"},"challenge":"Y2huWWtNcXNUaXZNMTM5NVl5ampna3RmMGxvZndpdi13ZEtLRTFBRFNCYy5NVGN4TWpBMk16RTRNRFV3T1EuY21saWJIY3pkV3h4Ym0wLmNqc29MQnk3NFpIQVJSdTlNSXd5bDBRY0k5VktSTFF3STlUaGN2UnBTN28\u003d","pubKeyCredParams":[{"alg":"-7","type":"public-key"},{"alg":"-35","type":"public-key"},{"alg":"-36","type":"public-key"},{"alg":"-257","type":"public-key"},{"alg":"-258","type":"public-key"},{"alg":"-259","type":"public-key"},{"alg":"-37","type":"public-key"},{"alg":"-38","type":"public-key"},{"alg":"-39","type":"public-key"},{"alg":"-1","type":"public-key"}],"rp":{"id":"ebay.es","name":"ebay.es"},"user":{"displayName":"some@user.com","id":"cmlibHczdWxxbm0\u003d","name":"some@user.com"}}
        """.trimIndent()

        val expected = """
            {"attestation":"direct","authenticatorSelection":{"authenticatorAttachment":"platform","requireResidentKey":true,"userVerification":"required"},"challenge":"Y2huWWtNcXNUaXZNMTM5NVl5ampna3RmMGxvZndpdi13ZEtLRTFBRFNCYy5NVGN4TWpBMk16RTRNRFV3T1EuY21saWJIY3pkV3h4Ym0wLmNqc29MQnk3NFpIQVJSdTlNSXd5bDBRY0k5VktSTFF3STlUaGN2UnBTN28=","pubKeyCredParams":[{"alg":-7,"type":"public-key"},{"alg":-35,"type":"public-key"},{"alg":-36,"type":"public-key"},{"alg":-257,"type":"public-key"},{"alg":-258,"type":"public-key"},{"alg":-259,"type":"public-key"},{"alg":-37,"type":"public-key"},{"alg":-38,"type":"public-key"},{"alg":-39,"type":"public-key"}],"rp":{"id":"ebay.es","name":"ebay.es"},"user":{"displayName":"some@user.com","id":"cmlibHczdWxxbm0=","name":"some@user.com"}}
        """.trimIndent()

        val res = PasskeyJsonSanitizer.sanitize(input)
        assertThat(res).isEqualTo(expected)
    }

}
