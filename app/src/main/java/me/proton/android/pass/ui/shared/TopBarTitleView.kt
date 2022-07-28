package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun TopBarTitleView(
    @StringRes title: Int
) {
    TopBarTitleView(stringResource(title))
}

@Composable
fun TopBarTitleView(
    title: String
) {
    Text(
        text = title,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        color = ProtonTheme.colors.textNorm,
    )
}
