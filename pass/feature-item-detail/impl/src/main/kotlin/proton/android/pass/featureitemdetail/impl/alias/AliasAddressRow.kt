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

package proton.android.pass.featureitemdetail.impl.alias

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
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle

@Composable
fun AliasAddressRow(
    modifier: Modifier = Modifier,
    alias: String,
    onCopyAlias: (String) -> Unit,
    onCreateLoginFromAlias: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopyAlias(alias) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
            contentDescription = null,
            tint = PassTheme.colors.aliasInteractionNorm
        )
        Column {
            SectionTitle(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(R.string.field_alias_title)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionSubtitle(
                modifier = Modifier.padding(start = 8.dp),
                text = alias.asAnnotatedString()
            )
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCreateLoginFromAlias(alias) }
                    .padding(8.dp),
                text = stringResource(R.string.alias_create_login_from_alias),
                color = PassTheme.colors.aliasInteractionNorm,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Preview
@Composable
fun AliasAddressRowPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AliasAddressRow(
                alias = "some@alias.test",
                onCopyAlias = {},
                onCreateLoginFromAlias = {}
            )
        }
    }
}
