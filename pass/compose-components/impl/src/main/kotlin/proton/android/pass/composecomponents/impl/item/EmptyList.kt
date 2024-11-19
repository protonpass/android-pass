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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.CircleButton

@Composable
fun EmptyList(
    modifier: Modifier = Modifier,
    emptyListMessage: String,
    canCreate: Boolean,
    onCreateItemClick: (() -> Unit)? = null,
    onOpenWebsiteClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder_bound_box),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (canCreate) {
            Text(
                text = stringResource(R.string.empty_list_title),
                style = ProtonTheme.typography.headlineNorm,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = emptyListMessage,
            style = ProtonTheme.typography.defaultWeak,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (canCreate && onCreateItemClick != null) {
                CircleButton(
                    contentPadding = ButtonDefaults.ContentPadding,
                    color = PassTheme.colors.interactionNormMajor1,
                    onClick = onCreateItemClick
                ) {
                    Text(
                        text = stringResource(R.string.empty_list_create_item_button),
                        style = PassTheme.typography.body3Norm(),
                        color = ProtonTheme.colors.textNorm
                    )
                }
            }
            if (onOpenWebsiteClick != null) {
                CircleButton(
                    contentPadding = ButtonDefaults.ContentPadding,
                    color = PassTheme.colors.interactionNormMinor1,
                    onClick = onOpenWebsiteClick
                ) {
                    Text(
                        text = stringResource(R.string.empty_list_open_extension),
                        style = PassTheme.typography.body3Norm(),
                        color = PassTheme.colors.interactionNormMajor1
                    )
                }
            }
        }
    }
}
