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

package proton.android.pass.features.sl.sync.management.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.overlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncAddButton
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncDescriptionText
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncLabelText
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncManagementMailboxSection(
    modifier: Modifier = Modifier,
    aliasMailboxes: ImmutableList<SimpleLoginAliasMailbox>,
    canManageAliases: Boolean,
    onAddClick: () -> Unit,
    onMenuClick: (SimpleLoginAliasMailbox) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            SimpleLoginSyncLabelText(
                modifier = Modifier.weight(1f, fill = true),
                text = stringResource(id = R.string.simple_login_sync_management_mailboxes_label)
            )

            SimpleLoginSyncAddButton(
                onClick = onAddClick
            )

            if (!canManageAliases) {
                PassPlusIcon()
            }
        }

        SimpleLoginSyncManagementMailboxes(
            aliasMailboxes = aliasMailboxes,
            onMenuClick = onMenuClick
        )

        SimpleLoginSyncDescriptionText(
            text = stringResource(id = R.string.simple_login_sync_management_mailboxes_description)
        )
    }
}

@Composable
private fun SimpleLoginSyncManagementMailboxes(
    modifier: Modifier = Modifier,
    aliasMailboxes: ImmutableList<SimpleLoginAliasMailbox>,
    onMenuClick: (SimpleLoginAliasMailbox) -> Unit
) {
    Column(
        modifier = modifier
            .roundedContainerNorm()
            .padding(vertical = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        aliasMailboxes.forEachIndexed { index, aliasMailbox ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(weight = 1f, fill = true)
                        .padding(start = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
                ) {
                    SectionSubtitle(
                        text = aliasMailbox.email.asAnnotatedString()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
                    ) {
                        if (aliasMailbox.isDefault) {
                            DefaultBadge()
                        }
                        val mailboxDescriptionText = when {
                            aliasMailbox.isVerified && !aliasMailbox.pendingEmail.isNullOrBlank() ->
                                stringResource(
                                    id = R.string.simple_login_sync_management_mailbox_unverified_change
                                )

                            aliasMailbox.isVerified -> pluralStringResource(
                                id = CompR.plurals.aliases_count,
                                count = aliasMailbox.aliasCount,
                                aliasMailbox.aliasCount
                            )

                            else -> stringResource(
                                id = R.string.simple_login_sync_management_mailbox_unverified
                            )
                        }
                        SectionTitle(
                            text = mailboxDescriptionText
                        )
                    }
                }

                ThreeDotsMenuButton(
                    onClick = {
                        onMenuClick(aliasMailbox)
                    }
                )
            }

            if (index < aliasMailboxes.lastIndex) {
                PassDivider()
            }
        }
    }
}

@Composable
private fun DefaultBadge() {
    Text(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = Radius.medium))
            .background(color = PassTheme.colors.interactionNormMajor2)
            .padding(
                horizontal = Spacing.small,
                vertical = Spacing.extraSmall
            ),
        text = stringResource(
            id = R.string.simple_login_sync_management_mailbox_default
        ),
        style = ProtonTheme.typography.overlineNorm,
        color = PassTheme.colors.textInvert
    )
}

@[Preview Composable]
internal fun SimpleLoginSyncMailboxSectionPreview(
    @PreviewParameter(ThemedSimpleLoginSyncMailboxSectionPreviewProvider::class)
    input: Pair<Boolean, SimpleLoginSyncMailboxSectionPreviewParams>
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncManagementMailboxSection(
                aliasMailboxes = params.aliasMailboxes,
                canManageAliases = params.canManageAliases,
                onAddClick = {},
                onMenuClick = {}
            )
        }
    }
}
