package me.proton.pass.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class PasswordGeneratorTest {

    @Test
    fun `can generate password with only letters`() {
        val res = PasswordGenerator.generatePassword(
            length = 5,
            option = PasswordGenerator.Option.Letters,
            random = Random(1234)
        )
        assertThat(res).isEqualTo("GyuhU")
    }

    @Test
    fun `can generate password with letters and numbers`() {
        val res = PasswordGenerator.generatePassword(
            length = 5,
            option = PasswordGenerator.Option.LettersAndNumbers,
            random = Random(1234)
        )
        assertThat(res).isEqualTo("9Tnft")
    }

    @Test
    fun `can generate password with letters numbers and symbols`() {
        val res = PasswordGenerator.generatePassword(
            length = 5,
            option = PasswordGenerator.Option.LettersNumbersSymbols,
            random = Random(1234)
        )
        assertThat(res).isEqualTo("55Jy+")
    }

    @Test
    fun `can generate passwords of many lengths`() {
        val expectedMap = mapOf(
            0 to mapOf(
                PasswordGenerator.Option.Letters to "",
                PasswordGenerator.Option.LettersAndNumbers to "",
                PasswordGenerator.Option.LettersNumbersSymbols to ""
            ),
            1 to mapOf(
                PasswordGenerator.Option.Letters to "G",
                PasswordGenerator.Option.LettersAndNumbers to "9",
                PasswordGenerator.Option.LettersNumbersSymbols to "9"
            ),
            2 to mapOf(
                PasswordGenerator.Option.Letters to "Gy",
                PasswordGenerator.Option.LettersAndNumbers to "9T",
                PasswordGenerator.Option.LettersNumbersSymbols to "9?"
            ),
            3 to mapOf(
                PasswordGenerator.Option.Letters to "Gyu",
                PasswordGenerator.Option.LettersAndNumbers to "9Tn",
                PasswordGenerator.Option.LettersNumbersSymbols to "55;"
            ),
            4 to mapOf(
                PasswordGenerator.Option.Letters to "Gyuh",
                PasswordGenerator.Option.LettersAndNumbers to "9Tnf",
                PasswordGenerator.Option.LettersNumbersSymbols to "55J."
            ),
            5 to mapOf(
                PasswordGenerator.Option.Letters to "GyuhU",
                PasswordGenerator.Option.LettersAndNumbers to "9Tnft",
                PasswordGenerator.Option.LettersNumbersSymbols to "55Jy+"
            )
        )

        val options = listOf(
            PasswordGenerator.Option.Letters,
            PasswordGenerator.Option.LettersAndNumbers,
            PasswordGenerator.Option.LettersNumbersSymbols
        )
        for (length in 0..5) {
            for (option in options) {
                val res = PasswordGenerator.generatePassword(
                    length = length,
                    option = option,
                    random = Random(1234)
                )
                assertThat(res.length).isEqualTo(length)
                val expected = expectedMap[length]?.get(option)
                assertThat(res).isEqualTo(expected)
            }
        }
    }
}
