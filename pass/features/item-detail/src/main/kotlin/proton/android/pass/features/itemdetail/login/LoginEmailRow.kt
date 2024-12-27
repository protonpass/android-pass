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

package proton.android.pass.features.itemdetail.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
internal fun LoginEmailRow(
    modifier: Modifier = Modifier,
    email: String,
    showViewAlias: Boolean,
    onEmailClick: () -> Unit,
    onGoToAliasClick: () -> Unit,
    @StringRes titleResId: Int = R.string.field_email,
    @DrawableRes iconResId: Int = CoreR.drawable.ic_proton_envelope
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEmailClick() }
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = PassTheme.colors.loginInteractionNorm
        )
        Column {
            SectionTitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = stringResource(id = titleResId)
            )

            Spacer(modifier = Modifier.height(height = Spacing.small))

            SectionSubtitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = email.asAnnotatedString()
            )

            if (showViewAlias) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(size = Radius.small))
                        .clickable { onGoToAliasClick() }
                        .padding(all = Spacing.small),
                    text = stringResource(R.string.login_item_view_alias_button),
                    color = PassTheme.colors.loginInteractionNorm,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@[Preview Composable]
internal fun LoginEmailRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, showViewAlias) = input

    PassTheme(isDark = isDark) {
        Surface {
            LoginEmailRow(
                email = "user@email.com",
                showViewAlias = showViewAlias,
                onEmailClick = {},
                onGoToAliasClick = {}
            )
        }
    }
}
