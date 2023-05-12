package proton.android.pass.featurepassword.impl.bottomsheet

import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.WordSeparator

sealed interface GeneratePasswordEvent {
    object OnRegeneratePasswordClick : GeneratePasswordEvent
    object OnPasswordModeChangeClick : GeneratePasswordEvent
    data class OnPasswordModeChange(val mode: PasswordGenerationMode) : GeneratePasswordEvent

    data class OnRandomLengthChange(val length: Int) : GeneratePasswordEvent
    data class OnRandomUseSpecialCharactersChange(val value: Boolean) : GeneratePasswordEvent
    data class OnRandomUseCapitalLettersChange(val value: Boolean) : GeneratePasswordEvent
    data class OnRandomIncludeNumbersChange(val value: Boolean) : GeneratePasswordEvent

    data class OnWordsCountChange(val count: Int) : GeneratePasswordEvent
    data class OnWordsCapitalizeChange(val value: Boolean) : GeneratePasswordEvent
    object OnWordsSeparatorClick : GeneratePasswordEvent
    data class OnWordsSeparatorChange(val separator: WordSeparator) : GeneratePasswordEvent
    data class OnWordsIncludeNumbersChange(val value: Boolean) : GeneratePasswordEvent
}
