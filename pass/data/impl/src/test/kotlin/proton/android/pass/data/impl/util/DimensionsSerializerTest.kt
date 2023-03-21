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
