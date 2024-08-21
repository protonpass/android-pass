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

package proton.android.pass.features.sl.sync.details.ui

import proton.android.pass.domain.ShareId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

internal sealed interface SimpleLoginSyncDetailsUiEvent {

    data object OnBackClicked : SimpleLoginSyncDetailsUiEvent

    data object OnDomainClicked : SimpleLoginSyncDetailsUiEvent

    data object OnMailboxClicked : SimpleLoginSyncDetailsUiEvent

    data object OnSyncSettingsClicked : SimpleLoginSyncDetailsUiEvent

    @JvmInline
    value class OnDefaultVaultClicked(internal val shareId: ShareId) : SimpleLoginSyncDetailsUiEvent

    data object OnOptionsDialogDismissed : SimpleLoginSyncDetailsUiEvent

    @JvmInline
    value class OnDomainSelected(
        internal val aliasDomain: SimpleLoginAliasDomain?
    ) : SimpleLoginSyncDetailsUiEvent

    @JvmInline
    value class OnMailboxSelected(
        internal val aliasMailbox: SimpleLoginAliasMailbox
    ) : SimpleLoginSyncDetailsUiEvent

    data object OnUpdateDomainClicked : SimpleLoginSyncDetailsUiEvent

    data object OnUpdateMailboxClicked : SimpleLoginSyncDetailsUiEvent

}
