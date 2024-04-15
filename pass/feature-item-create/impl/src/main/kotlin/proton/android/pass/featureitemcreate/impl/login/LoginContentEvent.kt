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

import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldEvent

sealed interface LoginContentEvent {
    data object Up : LoginContentEvent
    data class Submit(val shareId: ShareId) : LoginContentEvent
    data class OnUsernameChange(val username: String) : LoginContentEvent
    data class OnPasswordChange(val password: String) : LoginContentEvent
    data class OnWebsiteEvent(val event: WebsiteSectionEvent) : LoginContentEvent
    data class OnNoteChange(val note: String) : LoginContentEvent
    data class OnTotpChange(val totp: String) : LoginContentEvent
    data object PasteTotp : LoginContentEvent
    data class OnLinkedAppDelete(val app: PackageInfoUi) : LoginContentEvent
    data class OnCustomFieldEvent(val event: CustomFieldEvent) : LoginContentEvent
    data class OnFocusChange(val field: LoginField, val isFocused: Boolean) : LoginContentEvent
    data class OnDeletePasskey(val idx: Int, val passkey: UIPasskeyContent) : LoginContentEvent
}
