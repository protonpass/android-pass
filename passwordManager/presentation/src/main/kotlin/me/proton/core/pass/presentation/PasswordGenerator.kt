package me.proton.core.pass.presentation

sealed class PasswordGenerationOptions(
    val dictionary: String
) {
    companion object {
        private const val LETTERS = "abcdefhijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val SYMBOLS = ".?_-+*/"
    }

    object OnlyLetters: PasswordGenerationOptions(LETTERS)
    object LettersAndNumbers: PasswordGenerationOptions(LETTERS + NUMBERS)
    object LettersNumbersSymbols: PasswordGenerationOptions(LETTERS + NUMBERS + SYMBOLS)
}

fun generatePassword(
    length: Int,
    options: PasswordGenerationOptions = PasswordGenerationOptions.LettersAndNumbers
): String {
    val dictionary = options.dictionary
    var res = ""
    while (res.length < length) {
        res += dictionary.random()
    }
    return res
}