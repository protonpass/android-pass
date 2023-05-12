package proton.android.pass.featurepassword.impl.bottomsheet.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featurepassword.R

@Composable
fun GeneratePasswordWordsCountRow(
    modifier: Modifier = Modifier,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(SLIDER_TEXT_WEIGHT),
            text = stringResource(R.string.word_count, count),
            color = PassTheme.colors.textNorm,
            style = PassTypography.body3Regular,
            fontSize = 16.sp
        )

        var sliderPosition by remember { mutableStateOf(count.toFloat()) }
        val valueRange = remember { 1.toFloat()..10.toFloat() }
        Slider(
            modifier = Modifier.weight(SLIDER_CONTENT_WEIGHT),
            value = sliderPosition,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PassTheme.colors.loginInteractionNormMajor1,
                activeTrackColor = PassTheme.colors.loginInteractionNormMajor1,
                inactiveTrackColor = PassTheme.colors.loginInteractionNormMinor1
            ),
            onValueChange = { newLength ->
                if (sliderPosition.toInt() != newLength.toInt()) {
                    sliderPosition = newLength
                    onCountChange(newLength.toInt())
                }
            }
        )
    }
}

@Preview
@Composable
fun GeneratePasswordWordsCountRowPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordWordsCountRow(
                count = 4,
                onCountChange = {}
            )
        }
    }
}

private const val SLIDER_CONTENT_WEIGHT = 0.65f
private const val SLIDER_TEXT_WEIGHT = 0.35f
