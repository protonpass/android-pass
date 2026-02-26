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

package proton.android.pass.common.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileSizeUtilTest {

    @Test
    fun `returns 0 B for size 0`() {
        val result = FileSizeUtil.toHumanReadableSize(0)
        assertThat(result).isEqualTo("0 B")
    }

    @Test
    fun `returns 0 B for negative size`() {
        val result = FileSizeUtil.toHumanReadableSize(-1)
        assertThat(result).isEqualTo("0 B")
    }

    @Test
    fun `returns correct size for bytes less than 1024`() {
        val result = FileSizeUtil.toHumanReadableSize(500)
        assertThat(result).isEqualTo("500.0 B")
    }

    @Test
    fun `returns correct size for kilobytes`() {
        val result = FileSizeUtil.toHumanReadableSize(1_500)
        assertThat(result).isEqualTo("1.5 KB")
    }

    @Test
    fun `returns correct size for megabytes`() {
        val result = FileSizeUtil.toHumanReadableSize(1_048_576) // 1 MB = 1,024 * 1,024 bytes
        assertThat(result).isEqualTo("1.0 MB")
    }

    @Test
    fun `returns correct size for gigabytes`() {
        val result = FileSizeUtil.toHumanReadableSize(1_073_741_824) // 1 GB = 1,024 * 1,048,576 bytes
        assertThat(result).isEqualTo("1.0 GB")
    }

    @Test
    fun `returns correct size for terabytes`() {
        val result = FileSizeUtil.toHumanReadableSize(1_099_511_627_776) // 1 TB = 1,024 * 1,073,741,824 bytes
        assertThat(result).isEqualTo("1.0 TB")
    }

    @Test
    fun `handles rounding to one decimal place correctly`() {
        val result = FileSizeUtil.toHumanReadableSize(1_500_000)
        assertThat(result).isEqualTo("1.4 MB")
    }
}
