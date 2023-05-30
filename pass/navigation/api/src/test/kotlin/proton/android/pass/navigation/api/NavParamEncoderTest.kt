package proton.android.pass.navigation.api

import org.junit.Test
import kotlin.test.assertEquals

class NavParamEncoderTest {

    @Test
    fun `can encode content with forward slashes`() {
        val input = "a/content/with/slashes"
        val res = NavParamEncoder.encode(input)

        assertEquals("a%2Fcontent%2Fwith%2Fslashes", res)

        val decoded = NavParamEncoder.decode(res)
        assertEquals(input, decoded)
    }

    @Test
    fun `can encode content with emojis`() {
        val input = "a_content-with_emojis(\uD83D\uDD12)"
        val res = NavParamEncoder.encode(input)

        assertEquals("a_content-with_emojis%28%F0%9F%94%92%29", res)

        val decoded = NavParamEncoder.decode(res)
        assertEquals(input, decoded)
    }

    @Test
    fun `can encode content with special characters`() {
        val input = "Á còntént wïth 'special' ch@r4cters #! ñ"
        val res = NavParamEncoder.encode(input)

        assertEquals("%C3%81+c%C3%B2nt%C3%A9nt+w%C3%AFth+%27special%27+ch%40r4cters+%23%21+%C3%B1", res)

        val decoded = NavParamEncoder.decode(res)
        assertEquals(input, decoded)
    }

}
