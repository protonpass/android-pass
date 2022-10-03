package me.proton.core.pass.presentation.create.password

import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.PasswordGenerator

@Composable
internal fun PasswordText(
    modifier: Modifier = Modifier,
    password: String
) {
    val annotatedString = password
        .map {
            val color = when {
                PasswordGenerator.CharacterSet.NUMBERS.value.contains(it) -> ProtonTheme.colors.notificationError
                PasswordGenerator.CharacterSet.SYMBOLS.value.contains(it) -> ProtonTheme.colors.notificationSuccess
                else -> ProtonTheme.colors.textNorm
            }
            AnnotatedString(it.toString(), SpanStyle(color))
        }
        .reduceOrNull { acc, next -> acc.plus(next) }
        ?: AnnotatedString("")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = annotatedString,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6
        )
    }
}
