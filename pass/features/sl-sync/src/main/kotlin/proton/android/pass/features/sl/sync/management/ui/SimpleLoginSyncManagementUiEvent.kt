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

import proton.android.pass.domain.ShareId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

internal sealed interface SimpleLoginSyncManagementUiEvent {

    data object OnBackClicked : SimpleLoginSyncManagementUiEvent

    data object OnDomainClicked : SimpleLoginSyncManagementUiEvent

    @JvmInline
    value class OnSyncSettingsClicked(internal val shareId: ShareId?) : SimpleLoginSyncManagementUiEvent

    @JvmInline
    value class OnDefaultVaultClicked(internal val shareId: ShareId) : SimpleLoginSyncManagementUiEvent

    data object OnOptionsDialogDismissed : SimpleLoginSyncManagementUiEvent

    @JvmInline
    value class OnDomainSelected(
        internal val aliasDomain: SimpleLoginAliasDomain?
    ) : SimpleLoginSyncManagementUiEvent

    @JvmInline
    value class OnMailboxSelected(
        internal val aliasMailbox: SimpleLoginAliasMailbox
    ) : SimpleLoginSyncManagementUiEvent

    data object OnUpdateDomainClicked : SimpleLoginSyncManagementUiEvent

    data object OnUpdateMailboxClicked : SimpleLoginSyncManagementUiEvent

    data object OnAddMailboxClicked : SimpleLoginSyncManagementUiEvent

    @JvmInline
    value class OnMailboxMenuClicked(
        internal val aliasMailbox: SimpleLoginAliasMailbox
    ) : SimpleLoginSyncManagementUiEvent

}
