/*
 * Copyright (c) 2023-2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.features.itemcreate.R

@Composable
fun StickyUsernameOptions(
    modifier: Modifier = Modifier,
    primaryEmail: String?,
    showCreateAliasButton: Boolean,
    isExpanded: Boolean,
    onCreateAliasClick: () -> Unit,
    onPrefillCurrentEmailClick: (String) -> Unit,
    onGenerateUsernameClick: () -> Unit
) {
    if (!showCreateAliasButton && primaryEmail == null) return
    val focusManager = LocalFocusManager.current

    val items: List<@Composable () -> Unit> = buildList {
        if (showCreateAliasButton) {
            add {
                StickyUsernameAction(
                    icon = me.proton.core.presentation.R.drawable.ic_proton_alias,
                    text = stringResource(id = R.string.sticky_button_create_alias),
                    onClick = {
                        focusManager.clearFocus()
                        onCreateAliasClick()
                    }
                )
            }
        }
        if (!isExpanded) {
            add {
                StickyUsernameAction(
                    icon = me.proton.core.presentation.R.drawable.ic_proton_arrows_rotate,
                    text = stringResource(id = R.string.sticky_button_generate_username),
                    onClick = {
                        focusManager.clearFocus()
                        onGenerateUsernameClick()
                    }
                )
            }
        }
        if (primaryEmail != null) {
            add {
                StickyUsernameAction(
                    icon = null,
                    text = stringResource(id = R.string.sticky_button_use_account_email, primaryEmail),
                    onClick = {
                        focusManager.clearFocus()
                        onPrefillCurrentEmailClick(primaryEmail)
                    }
                )
            }
        }
    }

    val isScrollable = items.size >= 2

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(PassTheme.colors.backgroundNorm)
            .then(if (isScrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isScrollable) {
            Arrangement.spacedBy(8.dp)
        } else {
            Arrangement.Center
        }
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                StickyUsernameSeparator()
            }
            item()
        }
    }
}

@Composable
private fun StickyUsernameSeparator() {
    Divider(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 9.dp)
    )
}

@Composable
private fun StickyUsernameAction(
    @DrawableRes icon: Int?,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
        }
        Text(
            text = text,
            color = PassTheme.colors.loginInteractionNormMajor2,
            style = ProtonTheme.typography.defaultNorm,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

class ThemedStickyUsernamePreviewProvider :
    ThemePairPreviewProvider<StickyUsernameInput>(StickyUsernameOptionsPreviewProvider())

@Preview
@Composable
fun StickyUsernameOptionsPreview(
    @PreviewParameter(ThemedStickyUsernamePreviewProvider::class) input: Pair<Boolean, StickyUsernameInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            StickyUsernameOptions(
                primaryEmail = input.second.primaryEmail.value(),
                showCreateAliasButton = input.second.showCreateAlias,
                isExpanded = input.second.isExpanded,
                onCreateAliasClick = {},
                onPrefillCurrentEmailClick = {},
                onGenerateUsernameClick = {}
            )
        }
    }
}
