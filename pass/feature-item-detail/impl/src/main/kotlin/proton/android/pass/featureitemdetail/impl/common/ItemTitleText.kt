package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun ItemTitleText(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = 0.03.sp,
        lineHeight = 34.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
