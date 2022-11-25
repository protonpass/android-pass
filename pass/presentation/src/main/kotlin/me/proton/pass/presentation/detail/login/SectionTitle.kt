package me.proton.pass.presentation.detail.login

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun SectionTitle(
    @StringRes title: Int
) {
    Text(
        text = stringResource(title),
        fontSize = 16.sp,
        fontWeight = FontWeight.W400,
        color = ProtonTheme.colors.textNorm
    )
}

