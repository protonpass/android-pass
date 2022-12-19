package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun TopBarTitleView(
    modifier: Modifier = Modifier,
    @StringRes title: Int
) {
    TopBarTitleView(
        modifier = modifier,
        title = stringResource(title)
    )
}

@Composable
fun TopBarTitleView(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        color = ProtonTheme.colors.textNorm,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
