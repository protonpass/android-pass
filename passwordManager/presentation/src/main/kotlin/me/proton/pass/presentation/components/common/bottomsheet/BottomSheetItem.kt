package me.proton.pass.presentation.components.common.bottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun BottomSheetItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onItemClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick() })
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(title))
        Text(
            text = stringResource(title),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp),
            fontWeight = FontWeight.W400,
            color = ProtonTheme.colors.textNorm
        )
    }
}
