package proton.android.pass.presentation.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon

@Composable
fun NavigationDrawerVaultRow(
    modifier: Modifier = Modifier,
    name: String,
    itemCount: Long,
    @DrawableRes icon: Int,
    color: Color,
    isShared: Boolean,
    isSelected: Boolean,
    showMenuIcon: Boolean,
    onOptionsClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            color = color,
            icon = icon,
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                color = PassTheme.colors.textNorm,
            )
            Text(
                text = stringResource(R.string.navdrawer_vaults_item_count, itemCount),
                color = PassTheme.colors.textWeak,
            )
        }
        Box(modifier = Modifier.size(16.dp)) {
            if (isShared) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_users),
                    contentDescription = null
                )
            }
        }
        Box(modifier = Modifier.size(16.dp)) {
            if (isSelected) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_checkmark),
                    contentDescription = null,
                    tint = PassTheme.colors.accentPurpleOpaque
                )
            }
        }

        if (showMenuIcon) {
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = proton.android.pass.composecomponents.impl.R.drawable.ic_three_dots_vertical_24
                    ),
                    contentDescription = stringResource(
                        id = proton.android.pass.composecomponents.impl.R.string.action_content_description_menu
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

class ThemeVaultRowPreviewProvider :
    ThemePairPreviewProvider<VaultRowInput>(NavigationDrawerVaultRowPreviewProvider())

@Preview
@Composable
fun NavigationDrawerVaultRowPreview(
    @PreviewParameter(ThemeVaultRowPreviewProvider::class) input: Pair<Boolean, VaultRowInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NavigationDrawerVaultRow(
                name = "test vault",
                itemCount = 123,
                icon = me.proton.core.presentation.R.drawable.ic_proton_house,
                color = PassTheme.colors.accentYellowNorm,
                isSelected = input.second.isSelected,
                isShared = input.second.isShared,
                showMenuIcon = input.second.showMenuIcon,
                onOptionsClick = {},
                onClick = {}
            )
        }
    }
}
