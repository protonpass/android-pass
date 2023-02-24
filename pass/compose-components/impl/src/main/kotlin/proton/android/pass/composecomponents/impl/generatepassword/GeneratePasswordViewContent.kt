package proton.android.pass.composecomponents.impl.generatepassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadline
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.composecomponents.impl.R

@Composable
fun GeneratePasswordViewContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onSpecialCharactersChange: (Boolean) -> Unit,
    onLengthChange: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val annotatedString = state.password.toPasswordAnnotatedString(
            digitColor = PassTheme.colors.accentPurpleOpaque,
            symbolColor = PassTheme.colors.accentGreenOpaque,
            letterColor = PassTheme.colors.textNorm
        )
        Text(
            modifier = Modifier.height(100.dp),
            text = annotatedString,
            style = ProtonTheme.typography.subheadline
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.width(112.dp),
                text = stringResource(R.string.character_count, state.length)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val (length, setLength) = remember { mutableStateOf(state.length.toFloat()) }
            Slider(
                value = length,
                valueRange = 4.toFloat()..64.toFloat(),
                onValueChange = { newLength ->
                    if (length.toInt() != newLength.toInt()) {
                        setLength(newLength)
                        onLengthChange(newLength.toInt())
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.special_characters))
            Switch(
                checked = state.hasSpecialCharacters,
                onCheckedChange = { onSpecialCharactersChange(it) }
            )
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
                    length = 12,
                    hasSpecialCharacters = true
                ),
                onSpecialCharactersChange = {},
                onLengthChange = {}
            )
        }
    }
}
