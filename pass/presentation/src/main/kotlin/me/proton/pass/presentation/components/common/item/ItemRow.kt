package me.proton.pass.presentation.components.common.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun ItemRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: AnnotatedString,
    subtitle: AnnotatedString
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        icon()
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = ProtonTheme.colors.textWeak,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
