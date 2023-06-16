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

package proton.android.pass.data.impl.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DimensionsSerializerTest {

    @Test
    fun `can serialize empty map`() {
        val res = DimensionsSerializer.serialize(emptyMap())
        assertThat(res).isEqualTo("{}")
    }

    @Test
    fun `can serialize map with contents`() {
        val res = DimensionsSerializer.serialize(mapOf("key" to "value"))
        assertThat(res).isEqualTo("{\"key\":\"value\"}")
    }

    @Test
    fun `can deserialize empty map`() {
        val res = DimensionsSerializer.deserialize("{}")
        assertThat(res).isEmpty()
    }

    @Test
    fun `can deserialize map with contents`() {
        val res = DimensionsSerializer.deserialize("{\"key\":\"value\"}")
        assertThat(res.size).isEqualTo(1)

        val value = res.get("key")
        assertThat(value).isNotNull()
        assertThat(value!!.content).isEqualTo("value")
    }
}
