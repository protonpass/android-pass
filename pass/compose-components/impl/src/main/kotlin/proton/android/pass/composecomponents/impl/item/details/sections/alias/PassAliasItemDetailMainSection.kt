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

package proton.android.pass.composecomponents.impl.item.details.sections.alias

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.icon.ForwardIcon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.AliasMailbox
import me.proton.core.presentation.R as CoreR

@Composable
internal fun PassAliasItemDetailMainSection(
    modifier: Modifier = Modifier,
    alias: String,
    itemColors: ProtonItemColors,
    mailboxes: ImmutableList<AliasMailbox>,
    onSectionClick: (String, ItemDetailsFieldType.Plain) -> Unit,
) {
    val sections = mutableListOf<@Composable () -> Unit>()

    sections.add {
        PassAliasItemDetailAddressRow(
            alias = alias,
            itemColors = itemColors,
            onSectionClick = onSectionClick,
        )
    }

    sections.add {
        PassAliasItemDetailMailboxesRow(
            mailboxes = mailboxes,
            itemColors = itemColors,
        )
    }

    RoundedCornersColumn(modifier = modifier) {
        sections.forEachIndexed { index, block ->
            block()

            if (index < sections.lastIndex) {
                PassDivider()
            }
        }
    }
}

@Composable
private fun PassAliasItemDetailAddressRow(
    modifier: Modifier = Modifier,
    alias: String,
    itemColors: ProtonItemColors,
    onSectionClick: (String, ItemDetailsFieldType.Plain) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSectionClick(alias, ItemDetailsFieldType.Plain.Alias) }
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_alias),
            contentDescription = null,
            tint = itemColors.norm,
        )
        Column {
            SectionTitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = stringResource(R.string.item_details_alias_section_alias_title),
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            SectionSubtitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = alias.asAnnotatedString(),
            )

            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(Spacing.small))
                    .clickable { }
                    .padding(Spacing.small),
                text = stringResource(R.string.item_details_alias_section_create_login_link),
                color = itemColors.norm,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun PassAliasItemDetailMailboxesRow(
    modifier: Modifier = Modifier,
    mailboxes: ImmutableList<AliasMailbox>,
    itemColors: ProtonItemColors,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        ForwardIcon(tint = itemColors.norm)

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            SectionTitle(text = stringResource(R.string.item_details_alias_section_mailboxes_title))

            if (mailboxes.isEmpty()) {
                SectionSubtitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(),
                    text = AnnotatedString(text = ""),
                )
            } else {
                mailboxes.forEach { mailbox ->
                    SectionSubtitle(
                        text = mailbox.email.asAnnotatedString(),
                    )
                }
            }
        }
    }
}
