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

package proton.android.pass.featureitemcreate.impl.login

import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.tooltips.Tooltip
import proton.android.pass.featureitemcreate.impl.common.attachments.AttachmentContentEvent
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldEvent

internal sealed interface LoginContentEvent : AttachmentContentEvent {

    data object Up : LoginContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : LoginContentEvent

    @JvmInline
    value class OnEmailChanged(val email: String) : LoginContentEvent

    @JvmInline
    value class OnUsernameChanged(val username: String) : LoginContentEvent

    @JvmInline
    value class OnPasswordChange(val password: String) : LoginContentEvent

    @JvmInline
    value class OnWebsiteEvent(val event: WebsiteSectionEvent) : LoginContentEvent

    @JvmInline
    value class OnNoteChange(val note: String) : LoginContentEvent

    @JvmInline
    value class OnTotpChange(val totp: String) : LoginContentEvent

    data object PasteTotp : LoginContentEvent

    @JvmInline
    value class OnLinkedAppDelete(val app: PackageInfoUi) : LoginContentEvent

    @JvmInline
    value class OnCustomFieldEvent(val event: CustomFieldEvent) : LoginContentEvent

    data class OnFocusChange(val field: LoginField, val isFocused: Boolean) : LoginContentEvent

    data class OnDeletePasskey(val idx: Int, val passkey: UIPasskeyContent) : LoginContentEvent

    @JvmInline
    value class OnTitleChange(val title: String) : LoginContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : LoginContentEvent

    data class OnCreateAlias(
        val shareId: ShareId,
        val hasReachedAliasLimit: Boolean,
        val title: Option<String>
    ) : LoginContentEvent

    data object OnCreatePassword : LoginContentEvent

    data class OnAliasOptions(
        val shareId: ShareId,
        val hasReachedAliasLimit: Boolean
    ) : LoginContentEvent

    data object OnUpgrade : LoginContentEvent

    @JvmInline
    value class OnScanTotp(val index: Option<Int>) : LoginContentEvent

    @JvmInline
    value class OnTooltipDismissed(val tooltip: Tooltip) : LoginContentEvent

    data object OnUsernameOrEmailManuallyExpanded : LoginContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : LoginContentEvent
}
