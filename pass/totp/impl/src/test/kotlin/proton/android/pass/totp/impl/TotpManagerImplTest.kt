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
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.test.FixedClock
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

class TotpManagerImplTest {

    lateinit var instance: TotpManagerImpl

    @Before
    fun setup() {
        instance = TotpManagerImpl(FixedClock(Clock.System.now()))
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

}
