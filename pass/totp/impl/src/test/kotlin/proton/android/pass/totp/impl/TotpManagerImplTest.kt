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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import proton.android.pass.test.FixedClock
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import kotlin.time.Duration.Companion.seconds

class TotpManagerImplTest {

    private lateinit var instance: TotpManagerImpl

    private lateinit var clock: FixedClock

    @Before
    fun setup() {
        clock = FixedClock(Instant.fromEpochSeconds(NOW))
        instance = TotpManagerImpl(clock)
    }

    @Test
    fun `generateUri removes trailing slashes from label`() {
        val label = "thisIsMyLabel"
        val labelWithTrailingSlashes = "$label///"
        val res = instance.generateUri(
            TotpSpec(
                secret = "SECRET",
                label = labelWithTrailingSlashes,
                algorithm = TotpAlgorithm.Sha1,
                digits = TotpDigits.Six,
                validPeriodSeconds = 30,
            )
        )

        assertThat(res).isEqualTo(
            "otpauth://totp/$label/?digits=6&algorithm=SHA1&period=30&secret=SECRET"
        )
    }

    @Test
    fun `parse and generateUri respects the format`() {
        val source = "otpauth://totp/testLabel/?digits=6&algorithm=SHA1&period=30&secret=SECRET"

        val res = instance.generateUri(instance.parse(source).getOrThrow())
        assertThat(res).isEqualTo(source)
    }

    @Test
    fun `generateUri can handle secret with spaces`() {
        val root = "otpauth://totp/testLabel/?digits=6&algorithm=SHA1&period=30&secret="
        val source = "${root}ABCD EFGH"
        val expected = "${root}ABCDEFGH"

        val res = instance.generateUri(instance.parse(source).getOrThrow())
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `generateCode generates the expected code`() = runTest {
        val res = instance.observeCode(DEFAULT_SPEC).first()
        assertThat(res.code).isEqualTo(EXPECTED_CODE)
    }

    @Test
    fun `generateCode can handle secret with spaces`() = runTest {
        val spec = DEFAULT_SPEC.copy(secret = "SEC RE T")
        val res = instance.observeCode(spec).first()
        assertThat(res.code).isEqualTo(EXPECTED_CODE)
    }

    @Test
    fun `generateCode keeps generating the right code`() = runTest {
        instance.observeCode(DEFAULT_SPEC).test {
            val value1 = awaitItem()
            val expected1 = TotpManager.TotpWrapper(
                code = EXPECTED_CODE,
                remainingSeconds = 26,
                totalSeconds = DEFAULT_SPEC.validPeriodSeconds
            )
            assertThat(value1).isEqualTo(expected1)

            clock.updateInstant(clock.instant.plus(DEFAULT_SPEC.validPeriodSeconds.seconds))

            val value2 = awaitItem()
            val expected2 = expected1.copy(code = "408189")
            assertThat(value2).isEqualTo(expected2)
        }
    }

    companion object {
        private const val NOW = 123L
        private const val EXPECTED_CODE = "917204"
        private val DEFAULT_SPEC = TotpSpec(
            secret = "SECRET",
            label = "label",
            algorithm = TotpAlgorithm.Sha1,
            digits = TotpDigits.Six,
            validPeriodSeconds = 30,
        )
    }

}
