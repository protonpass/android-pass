package proton.android.pass.featureitemcreate.impl.creditcard

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
