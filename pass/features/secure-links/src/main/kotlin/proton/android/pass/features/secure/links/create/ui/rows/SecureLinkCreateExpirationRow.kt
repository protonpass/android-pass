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

package proton.android.pass.features.secure.links.create.ui.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.create.ui.SecureLinksCreateUiEvent
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecureLinkCreateExpirationRow(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .clickable {
                onUiEvent(SecureLinksCreateUiEvent.OnSetExpirationClicked)
            }
            .padding(
                start = Spacing.medium,
                top = Spacing.mediumSmall,
                end = Spacing.medium
            ),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = CoreR.drawable.ic_proton_clock),
                contentDescription = null,
                tint = PassTheme.colors.textNorm
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false),
                text = stringResource(id = R.string.secure_links_create_row_expiration_title),
                style = ProtonTheme.typography.body1Regular
            )

            Icon(
                painter = painterResource(id = CompR.drawable.ic_chevron_tiny_right),
                contentDescription = null,
                tint = PassTheme.colors.textNorm
            )
        }

        Row(
            modifier = Modifier.offset(y = -Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = CoreR.drawable.ic_proton_clock),
                contentDescription = null,
                tint = PassTheme.colors.backgroundStrong
            )

            Text(
                text = "7 days",
                style = ProtonTheme.typography.body2Regular,
                color = PassTheme.colors.textWeak
            )
        }
    }
}
