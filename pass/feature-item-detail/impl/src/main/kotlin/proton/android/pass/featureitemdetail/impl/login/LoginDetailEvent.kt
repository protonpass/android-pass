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

package proton.android.pass.featureitemdetail.impl.login

import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.featureitemdetail.impl.login.customfield.CustomFieldEvent

internal sealed interface LoginDetailEvent {

    data object OnTogglePasswordClick : LoginDetailEvent

    @JvmInline
    value class OnEmailClick(val email: String) : LoginDetailEvent

    data object OnUsernameClick : LoginDetailEvent

    data object OnGoToAliasClick : LoginDetailEvent

    data object OnCopyPasswordClick : LoginDetailEvent

    @JvmInline
    value class OnWebsiteClicked(val website: String) : LoginDetailEvent

    @JvmInline
    value class OnWebsiteLongClicked(val website: String) : LoginDetailEvent

    @JvmInline
    value class OnCopyTotpClick(val totpCode: String) : LoginDetailEvent

    data object OnUpgradeClick : LoginDetailEvent

    data object OnShareClick : LoginDetailEvent

    @JvmInline
    value class OnCustomFieldEvent(val event: CustomFieldEvent) : LoginDetailEvent

    data object OnViewItemHistoryClicked : LoginDetailEvent

    @JvmInline
    value class OnSelectPasskey(val passkey: UIPasskeyContent) : LoginDetailEvent

    data object OnShowReusedPasswords : LoginDetailEvent

    @JvmInline
    value class OnAttachmentEvent(
        val attachmentContentEvent: AttachmentContentEvent
    ) : LoginDetailEvent
}
