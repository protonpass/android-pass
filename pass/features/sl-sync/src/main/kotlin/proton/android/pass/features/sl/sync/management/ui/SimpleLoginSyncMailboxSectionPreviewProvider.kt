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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

internal class SimpleLoginSyncMailboxSectionPreviewProvider :
    PreviewParameterProvider<SimpleLoginSyncMailboxSectionPreviewParams> {

    private val canManageAliases = listOf(false, true)

    override val values: Sequence<SimpleLoginSyncMailboxSectionPreviewParams> = sequence {
        for (canManageAlias in canManageAliases) {
            persistentListOf(
                SimpleLoginAliasMailbox(
                    id = 1L,
                    email = "user_default@email.com",
                    pendingEmail = null,
                    isDefault = true,
                    isVerified = true,
                    aliasCount = 5
                ),
                SimpleLoginAliasMailbox(
                    id = 1L,
                    email = "user_verified@email.com",
                    pendingEmail = null,
                    isDefault = false,
                    isVerified = true,
                    aliasCount = 1
                ),
                SimpleLoginAliasMailbox(
                    id = 1L,
                    email = "user_unverified@email.com",
                    pendingEmail = null,
                    isDefault = false,
                    isVerified = false,
                    aliasCount = 0
                )
            ).also { aliasMailboxes ->
                yield(
                    value = SimpleLoginSyncMailboxSectionPreviewParams(
                        aliasMailboxes = aliasMailboxes,
                        canManageAliases = canManageAlias
                    )
                )
            }
        }
    }
}

internal data class SimpleLoginSyncMailboxSectionPreviewParams(
    internal val aliasMailboxes: ImmutableList<SimpleLoginAliasMailbox>,
    internal val canManageAliases: Boolean
)

internal class ThemedSimpleLoginSyncMailboxSectionPreviewProvider :
    ThemePairPreviewProvider<SimpleLoginSyncMailboxSectionPreviewParams>(
        provider = SimpleLoginSyncMailboxSectionPreviewProvider()
    )
