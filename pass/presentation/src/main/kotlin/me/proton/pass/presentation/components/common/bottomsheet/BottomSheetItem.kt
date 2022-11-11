package me.proton.pass.presentation.components.common.bottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@Composable
fun BottomSheetItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
    onItemClick: () -> Unit
) {
    BottomSheetItem(
        modifier = modifier,
        icon = { Icon(painter = painterResource(icon), contentDescription = stringResource(title)) },
        title = title,
        subtitle = subtitle,
        onItemClick = onItemClick
    )
}

@Composable
fun BottomSheetItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)?,
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
    onItemClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick() })
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        icon?.let { it() }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            Text(
                text = stringResource(title),
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm
            )
            subtitle?.let {
                Text(
                    text = stringResource(it),
                    style = ProtonTheme.typography.defaultSmall,
                    color = ProtonTheme.colors.textWeak
                )
            }
        }
    }
}

@Preview
@Composable
fun BottomSheetItemPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            BottomSheetItem(
                icon = me.proton.core.presentation.R.drawable.ic_proton_key,
                title = me.proton.pass.domain.R.string.item_type_login,
                subtitle = R.string.item_type_login_description,
                onItemClick = {}
            )
        }
    }
}
