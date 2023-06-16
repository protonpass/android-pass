/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle

@Composable
fun LoginUsernameRow(
    modifier: Modifier = Modifier,
    username: String,
    showViewAlias: Boolean,
    onUsernameClick: () -> Unit,
    onGoToAliasClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUsernameClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_user),
            contentDescription = null,
            tint = PassTheme.colors.loginInteractionNorm
        )
        Column {
            SectionTitle(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(R.string.field_username)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionSubtitle(
                modifier = Modifier.padding(start = 8.dp),
                text = username.asAnnotatedString()
            )

            if (showViewAlias) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onGoToAliasClick() }
                        .padding(8.dp),
                    text = stringResource(R.string.login_item_view_alias_button),
                    color = PassTheme.colors.loginInteractionNorm,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Preview
@Composable
fun LoginUsernameRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginUsernameRow(
                username = "some.username",
                showViewAlias = input.second,
                onUsernameClick = {},
                onGoToAliasClick = {},
            )
        }
    }
}
