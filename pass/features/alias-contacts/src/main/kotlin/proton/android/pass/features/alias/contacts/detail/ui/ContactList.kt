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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.aliascontacts.R

@Composable
fun ContactList(
    modifier: Modifier = Modifier,
    blockedContacts: ImmutableList<Contact>,
    forwardingContacts: ImmutableList<Contact>,
    contactBlockIsLoading: PersistentSet<ContactId>,
    canSendEmail: Boolean,
    onEvent: (DetailAliasContactUIEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)) {
        if (forwardingContacts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text.Body2Regular(stringResource(R.string.forwarding_addresses_subtitle))
            forwardingContacts.forEach { contact ->
                ContactRow(
                    contact = contact,
                    isBlockLoading = contactBlockIsLoading.contains(contact.id),
                    canSendEmail = canSendEmail,
                    onEvent = onEvent
                )
            }
        }

        if (blockedContacts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text.Body3Regular(
                modifier = Modifier.padding(horizontal = Spacing.small),
                text = stringResource(R.string.blocked_addresses_subtitle),
                color = PassTheme.colors.textWeak
            )
            blockedContacts.forEach { contact ->
                ContactRow(
                    contact = contact,
                    isBlockLoading = contactBlockIsLoading.contains(contact.id),
                    canSendEmail = canSendEmail,
                    onEvent = onEvent
                )
            }
        }
    }
}

