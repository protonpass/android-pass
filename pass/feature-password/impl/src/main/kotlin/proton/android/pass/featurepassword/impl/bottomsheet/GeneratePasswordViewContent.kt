package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.featurepassword.impl.bottomsheet.random.GeneratePasswordRandomContent
import proton.android.pass.featurepassword.impl.bottomsheet.words.GeneratePasswordWordsContent

@Composable
fun GeneratePasswordViewContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onEvent: (GeneratePasswordEvent) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val annotatedString = state.password.toPasswordAnnotatedString(
            digitColor = PassTheme.colors.loginInteractionNormMajor2,
            symbolColor = PassTheme.colors.aliasInteractionNormMajor2,
            letterColor = PassTheme.colors.textNorm
        )
        Text(
            modifier = Modifier
                .height(100.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = annotatedString,
            style = ProtonTheme.typography.subheadlineNorm
        )
        when (state.content) {
            is GeneratePasswordContent.RandomPassword -> {
                GeneratePasswordRandomContent(
                    content = state.content,
                    onEvent = onEvent
                )
            }
            is GeneratePasswordContent.WordsPassword -> {
                GeneratePasswordWordsContent(
                    content = state.content,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Preview
@Composable
fun GeneratePasswordViewContentThemePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            GeneratePasswordViewContent(
                state = GeneratePasswordUiState(
                    password = "a1b!c_d3e#fg",
                    mode = GeneratePasswordMode.CopyAndClose,
                    content = GeneratePasswordContent.RandomPassword(
                        length = 12,
                        hasSpecialCharacters = true,
                        hasCapitalLetters = false,
                        includeNumbers = true
                    )
                ),
                onEvent = {}
            )
        }
    }
}
