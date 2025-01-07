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

package proton.android.pass.features.itemdetail.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.SectionSubtitle
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AliasAddressRow(
    modifier: Modifier = Modifier,
    alias: String,
    isAliasEnabled: Boolean,
    isAliasSyncEnabled: Boolean,
    isAliasStateToggling: Boolean,
    onCopyAlias: (String) -> Unit,
    onCreateLoginFromAlias: (String) -> Unit,
    onToggleAliasState: (Boolean) -> Unit
) {
    val titleResourceId = remember(isAliasEnabled) {
        if (isAliasEnabled) {
            R.string.field_alias_title
        } else {
            R.string.field_alias_disabled_title
        }
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCopyAlias(alias) }
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_envelope),
                contentDescription = null,
                tint = PassTheme.colors.aliasInteractionNorm
            )
            Column(Modifier.weight(1f)) {
                SectionTitle(
                    modifier = Modifier.padding(start = Spacing.small),
                    text = stringResource(id = titleResourceId)
                )

                Spacer(modifier = Modifier.height(8.dp))

                SectionSubtitle(
                    modifier = Modifier.padding(start = Spacing.small),
                    text = alias.asAnnotatedString()
                )
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(size = Radius.small))
                        .clickable { onCreateLoginFromAlias(alias) }
                        .padding(Spacing.small),
                    text = stringResource(R.string.alias_create_login_from_alias),
                    color = PassTheme.colors.aliasInteractionNorm,
                    textDecoration = TextDecoration.Underline
                )
            }

            if (isAliasSyncEnabled) {
                if (isAliasStateToggling) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = Spacing.small)
                            .size(size = 32.dp),
                        color = PassTheme.colors.aliasInteractionNorm
                    )
                } else {
                    Switch(
                        checked = isAliasEnabled,
                        onCheckedChange = onToggleAliasState,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PassTheme.colors.aliasInteractionNorm,
                            checkedTrackColor = PassTheme.colors.aliasInteractionNormMajor1
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun AliasAddressRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isAliasEnabled) = input

    PassTheme(isDark = isDark) {
        Surface {
            AliasAddressRow(
                alias = "some@alias.test",
                isAliasEnabled = isAliasEnabled,
                isAliasSyncEnabled = true,
                isAliasStateToggling = false,
                onCopyAlias = {},
                onCreateLoginFromAlias = {},
                onToggleAliasState = {}
            )
        }
    }
}
