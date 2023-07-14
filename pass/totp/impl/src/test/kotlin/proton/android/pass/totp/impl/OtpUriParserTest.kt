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

package proton.android.pass.totp.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.totp.api.MalformedOtpUri
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

class OtpUriParserTest {

    @Test
    fun `can parse uri with all parameters`() {
        val input =
            "otpauth://totp/thisisalabel?secret=somerandomsecret&issuer=theissuer&algorithm=SHA256&digits=8&period=24"
        val expected = TotpSpec(
            label = "thisisalabel",
            secret = "somerandomsecret",
            issuer = "theissuer".some(),
            algorithm = TotpAlgorithm.Sha256,
            digits = TotpDigits.Eight,
            validPeriodSeconds = 24
        )

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.success(expected))
    }

    @Test
    fun `can parse reference uri`() {
        val input =
            "otpauth://totp/thisisthelabel?secret=thisisthesecret&algorithm=SHA1&digits=6&period=10"
        val expected = TotpSpec(
            label = "thisisthelabel",
            secret = "thisisthesecret",
            issuer = None,
            algorithm = TotpAlgorithm.Sha1,
            digits = TotpDigits.Six,
            validPeriodSeconds = 10
        )

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.success(expected))
    }

    @Test
    fun `can add all default parameters`() {
        val input = "otpauth://totp/thisisthelabel?secret=thisisthesecret"
        val expected = TotpSpec(
            label = "thisisthelabel",
            secret = "thisisthesecret",
            issuer = None,
            algorithm = TotpAlgorithm.Sha1,
            digits = TotpDigits.Six,
            validPeriodSeconds = 30
        )

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.success(expected))
    }

    @Test
    fun `can detect missing scheme`() {
        val input = "totp/thisisthelabel?secret=thisisthesecret"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.MissingScheme))
    }

    @Test
    fun `can detect invalid scheme`() {
        val input = "wrong://totp/thisisthelabel?secret=thisisthesecret"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(
            Result.failure<TotpSpec>(MalformedOtpUri.InvalidScheme("wrong"))
        )
    }

    @Test
    fun `can detect missing host`() {
        val input = "otpauth:///thisisthelabel?secret=thisisthesecret"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.MissingHost))
    }

    @Test
    fun `can detect invalid host`() {
        val input = "otpauth://invalid/thisisthelabel?secret=thisisthesecret"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.InvalidHost("invalid")))
    }

    @Test
    fun `adds default label if missing label`() {
        val input = "otpauth://totp?secret=thisisthesecret"

        val expected = TotpSpec(
            label = OtpUriParser.DEFAULT_LABEL,
            secret = "thisisthesecret",
            issuer = None,
            algorithm = TotpAlgorithm.Sha1,
            digits = TotpDigits.Six,
            validPeriodSeconds = 30
        )

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.success(expected))
    }

    @Test
    fun `can detect missing secret`() {
        val input = "otpauth://totp/thisisthelabel?digits=6"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.MissingSecret))
    }

    @Test
    fun `can detect invalid algorithm`() {
        val input = "otpauth://totp/thisisthelabel?secret=thisisthesecret&algorithm=wrong"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.InvalidAlgorithm("wrong")))
    }

    @Test
    fun `can detect invalid digit count (number)`() {
        val input = "otpauth://totp/thisisthelabel?secret=thisisthesecret&digits=300"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.InvalidDigitCount("300")))
    }

    @Test
    fun `can detect invalid digit count (not number)`() {
        val input = "otpauth://totp/thisisthelabel?secret=thisisthesecret&digits=notanumber"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.InvalidDigitCount("notanumber")))
    }

    @Test
    fun `can detect invalid period`() {
        val input = "otpauth://totp/thisisthelabel?secret=thisisthesecret&period=notanumber"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed).isEqualTo(Result.failure<TotpSpec>(MalformedOtpUri.InvalidValidity("notanumber")))
    }

    @Test
    fun `cleans semicolon from label`() {
        val input = "otpauth://totp/first:second?secret=JBSWY3DPEHPK3PXP&period=30&digits=6&issuer=asdf"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed.isSuccess).isTrue()

        val spec = parsed.getOrThrow()
        assertThat(spec.label).isEqualTo("firstsecond")
    }

    @Test
    fun `can handle literal spaces on uri`() {
        val input = "otpauth://totp/first:second?secret=ABCD EFGH"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed.isSuccess).isTrue()

        val spec = parsed.getOrThrow()
        assertThat(spec.secret).isEqualTo("ABCDEFGH")
    }

    @Test
    fun `can handle urlencoded spaces on uri`() {
        val input = "otpauth://totp/first:second?secret=ABCD%20EFGH"

        val parsed = OtpUriParser.parse(input)
        assertThat(parsed.isSuccess).isTrue()

        val spec = parsed.getOrThrow()
        assertThat(spec.secret).isEqualTo("ABCDEFGH")
    }
}
