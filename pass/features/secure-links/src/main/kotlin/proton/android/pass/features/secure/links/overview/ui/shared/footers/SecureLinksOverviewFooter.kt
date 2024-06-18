/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.secure.links.overview.ui.shared.footers

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.features.secure.links.R

@Composable
internal fun SecureLinksOverviewFooter(
    modifier: Modifier = Modifier,
    @StringRes linkTextResId: Int,
    onCopyLinkClicked: () -> Unit,
    onShareLinkClicked: () -> Unit,
    onLinkClicked: () -> Unit,
    linkTextColor: Color = PassTheme.colors.interactionNormMajor2
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PassCircleButton(
            text = stringResource(id = R.string.secure_links_overview_button_copy_link),
            onClick = onCopyLinkClicked
        )

        PassCircleButton(
            text = stringResource(id = R.string.secure_links_overview_button_share_link),
            textColor = PassTheme.colors.interactionNormMajor2,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            onClick = onShareLinkClicked
        )

        TextButton(
            onClick = onLinkClicked
        ) {
            Text(
                text = stringResource(id = linkTextResId),
                style = ProtonTheme.typography.defaultNorm,
                color = linkTextColor
            )
        }
    }
}
