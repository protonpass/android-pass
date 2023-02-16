package proton.android.pass.featurecreateitem.impl.password

import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.toPasswordAnnotatedString

@Composable
fun PasswordText(
    modifier: Modifier = Modifier,
    text: String
) {
    val annotatedString = text.toPasswordAnnotatedString(
        digitColor = ProtonTheme.colors.notificationError,
        symbolColor = ProtonTheme.colors.notificationSuccess,
        letterColor = ProtonTheme.colors.textNorm
    )

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
