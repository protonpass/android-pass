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

package proton.android.pass.data.impl.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Sha1Test {

    @Test
    fun `sha1Hex returns uppercase hex digest`() {
        assertThat(sha1Hex("hunter2")).isEqualTo("F3BBBD66A63D4BF1747940578EC3D0103530E21D")
    }

    @Test
    fun `sha1Hex of empty string`() {
        assertThat(sha1Hex("")).isEqualTo("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709")
    }
}
