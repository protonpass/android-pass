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

package proton.android.pass.features.itemcreate.creditcard

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ExpirationDateProtoMapperTest {

    @Test
    fun `toProto converts MMYY to YYYY-MM format`() {
        val input = "0122"

        val result = ExpirationDateProtoMapper.toProto(input)

        assertEquals("2022-01", result)
    }

    @Test
    fun `toProto handles short input strings`() {
        val input = "5"

        val result = ExpirationDateProtoMapper.toProto(input)

        assertEquals("", result)
    }

    @Test
    fun `toProto handles invalid input`() {
        val input = "invalid-format"

        val result = ExpirationDateProtoMapper.toProto(input)

        assertEquals("", result)
    }

    @Test
    fun `toProto handles long input strings`() {
        val input = "1234567890"

        val result = ExpirationDateProtoMapper.toProto(input)

        assertEquals("", result)
    }

    @Test
    fun `fromProto converts YYYY-MM to MMYY format`() {
        val input = "2022-01"

        val result = ExpirationDateProtoMapper.fromProto(input)

        assertEquals("0122", result)
    }

    @Test
    fun `fromProto handles invalid input`() {
        val input = "invalid-format"

        val result = ExpirationDateProtoMapper.fromProto(input)

        assertEquals("", result)
    }
}
