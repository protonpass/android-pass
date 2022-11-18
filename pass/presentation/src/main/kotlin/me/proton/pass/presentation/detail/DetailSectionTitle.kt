package me.proton.pass.presentation.detail

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun DetailSectionTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        color = ProtonTheme.colors.textNorm
    )
}
