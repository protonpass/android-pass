package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun AliasPrefixSuffixText(
    modifier: Modifier = Modifier,
    prefix: String,
    suffix: String,
    prefixColor: Color = PassTheme.colors.textNorm,
    suffixColor: Color,
    fontSize: TextUnit = 16.sp
) {
    val value = buildAnnotatedString {
        append(AnnotatedString(prefix, SpanStyle(prefixColor)))
        append(AnnotatedString(suffix, SpanStyle(suffixColor)))
    }

    Text(
        modifier = modifier,
        text = value,
        fontSize = fontSize,
    )
}

@Preview
@Composable
fun AliasPrefixSuffixTextPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasPrefixSuffixText(
                prefix = "some.prefix",
                suffix = ".some@suffix.test",
                suffixColor = PassTheme.colors.accentGreenNorm
            )
        }
    }
}
