package proton.android.pass.featurehome.impl

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    onListClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomNavigation(
        modifier = modifier,
        backgroundColor = PassTheme.colors.backgroundStrong
    ) {
        BottomNavigationItem(
            selected = true,
            onClick = onListClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_list_bullets),
                    contentDescription = stringResource(
                        proton.android.pass.composecomponents.impl.R.string.alias_title_icon_content_description
                    ),
                    tint = PassTheme.colors.accentBrandNorm
                )
            }
        )
        BottomNavigationItem(
            selected = false,
            onClick = onCreateClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_plus),
                    contentDescription = stringResource(
                        proton.android.pass.composecomponents.impl.R.string.alias_title_icon_content_description
                    ),
                    tint = Color.White
                )
            }
        )
        BottomNavigationItem(
            selected = false,
            onClick = onProfileClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_user),
                    contentDescription = stringResource(
                        proton.android.pass.composecomponents.impl.R.string.alias_title_icon_content_description
                    ),
                    tint = Color.White
                )
            }
        )
    }
}


@Preview
@Composable
fun BottomBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            BottomBar(onListClick = {}, onCreateClick = {}, onProfileClick = {})
        }
    }
}
