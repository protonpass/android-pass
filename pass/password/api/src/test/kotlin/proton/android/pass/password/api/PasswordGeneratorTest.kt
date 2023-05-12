package proton.android.pass.password.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.password.api.PasswordGenerator.containsCapitalLetters
import proton.android.pass.password.api.PasswordGenerator.containsNumbers
import proton.android.pass.password.api.PasswordGenerator.containsSymbols
import kotlin.random.Random

class PasswordGeneratorTest {

    @Test
    fun `4 characters with no capital letters, no numbers no symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = false,
                hasNumbers = false,
                hasSymbols = false
            ),
            expected = "gyuh"
        )
    }

    @Test
    fun `4 characters with yes capital letters, no numbers no symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = true,
                hasNumbers = false,
                hasSymbols = false
            ),
            expected = "Gyuh"
        )
    }

    @Test
    fun `4 characters with no capital letters, yes numbers no symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = false,
                hasNumbers = true,
                hasSymbols = false
            ),
            expected = "b1jd"
        )
    }

    @Test
    fun `4 characters with no capital letters, no numbers yes symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = false,
                hasNumbers = false,
                hasSymbols = true
            ),
            expected = "!%!w"
        )
    }

    @Test
    fun `4 characters with yes capital letters, yes numbers no symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = true,
                hasNumbers = true,
                hasSymbols = false
            ),
            expected = "ZG1f"
        )
    }


    @Test
    fun `4 characters with yes capital letters, no numbers yes symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = true,
                hasNumbers = false,
                hasSymbols = true
            ),
            expected = "wYV*"
        )
    }

    @Test
    fun `4 characters with no capital letters, yes numbers yes symbols`() {
        test(
            spec = PasswordGenerator.RandomPasswordSpec(
                length = 4,
                hasCapitalLetters = false,
                hasNumbers = true,
                hasSymbols = true
            ),
            expected = "ra1*"
        )
    }

    @Test
    fun `multiple characters`() {
        val cases = mapOf(
            5 to "fN\$9&",
            6 to "fN\$*8y",
            7 to "fN\$*&3!",
            8 to "fN\$*&y5D",
            9 to "fN\$*&y!1^",
        )
        cases.forEach { (length, expected) ->
            test(
                spec = PasswordGenerator.RandomPasswordSpec(
                    length = length,
                    hasCapitalLetters = true,
                    hasNumbers = true,
                    hasSymbols = true
                ),
                expected = expected
            )
        }
    }


    private fun test(spec: PasswordGenerator.RandomPasswordSpec, expected: String): String {
        val res = PasswordGenerator.generatePassword(
            spec = spec,
            random = Random(1234)
        )
        assertThat(res.length).isEqualTo(spec.length)
        assertThat(res).isEqualTo(expected)

        assertThat(res.containsCapitalLetters()).isEqualTo(spec.hasCapitalLetters)
        assertThat(res.containsNumbers()).isEqualTo(spec.hasNumbers)
        assertThat(res.containsSymbols()).isEqualTo(spec.hasSymbols)

        return res
    }
}

