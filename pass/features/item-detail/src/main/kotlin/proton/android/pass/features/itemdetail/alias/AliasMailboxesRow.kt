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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.icon.ForwardIcon
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.modifiers.placeholder
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.SectionSubtitle
import proton.android.pass.domain.AliasMailbox

@Composable
fun AliasMailboxesRow(
    modifier: Modifier = Modifier,
    mailboxes: ImmutableList<AliasMailbox>,
    isLoading: Boolean
) {
    if (!isLoading && mailboxes.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ForwardIcon(tint = PassTheme.colors.aliasInteractionNorm)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(text = stringResource(R.string.alias_detail_field_mailboxes_title))
            if (mailboxes.isEmpty() && isLoading) {
                SectionSubtitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(),
                    text = AnnotatedString("")
                )
            } else {
                mailboxes.forEach { mailbox ->
                    SectionSubtitle(text = mailbox.email.asAnnotatedString())
                }
            }
        }
    }
}
