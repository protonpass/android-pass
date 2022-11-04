package me.proton.pass.presentation.components.form

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption

@Composable
fun ProtonTextTitle(
    @StringRes title: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(title),
        color = ProtonTheme.colors.textNorm,
        style = ProtonTheme.typography.caption,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        modifier = modifier
    )
}
