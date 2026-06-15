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

package proton.android.pass.commonuimodels.api.masks

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val LTR_ISOLATE = "\u2066"
private const val POP_DIRECTIONAL_ISOLATE = "\u2069"

class TextMaskTest {

    @Test
    fun `TotpCode splits a 6 digit code into two halves`() {
        val masked = TextMask.TotpCode("202671").masked

        assertThat(masked).isEqualTo("${LTR_ISOLATE}202 • 671$POP_DIRECTIONAL_ISOLATE")
    }

    @Test
    fun `TotpCode splits an 8 digit code into two halves`() {
        val masked = TextMask.TotpCode("12345678").masked

        assertThat(masked).isEqualTo("${LTR_ISOLATE}1234 • 5678$POP_DIRECTIONAL_ISOLATE")
    }

    @Test
    fun `TotpCode wraps the masked code in a left-to-right isolate`() {
        val masked = TextMask.TotpCode("202671").masked

        assertThat(masked.first().toString()).isEqualTo(LTR_ISOLATE)
        assertThat(masked.last().toString()).isEqualTo(POP_DIRECTIONAL_ISOLATE)
    }

    @Test
    fun `TotpCode unmasked keeps the raw code`() {
        val mask = TextMask.TotpCode("202671")

        assertThat(mask.unmasked).isEqualTo("202671")
    }
}
