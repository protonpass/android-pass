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

package proton.android.pass.composecomponents.impl.item.details

import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ShareId

sealed interface PassItemDetailsUiEvent {

    data class OnSectionClick(
        val section: String,
        val field: ItemDetailsFieldType.Plain
    ) : PassItemDetailsUiEvent

    data class OnHiddenFieldClick(
        val state: HiddenState,
        val field: ItemDetailsFieldType.Hidden
    ) : PassItemDetailsUiEvent

    data class OnHiddenFieldToggle(
        val isVisible: Boolean,
        val hiddenState: HiddenState,
        val fieldType: ItemDetailsFieldType.Hidden,
        val fieldSection: ItemSection
    ) : PassItemDetailsUiEvent

    @JvmInline
    value class OnLinkClick(val link: String) : PassItemDetailsUiEvent

    @JvmInline
    value class OnPasskeyClick(val passkey: UIPasskeyContent) : PassItemDetailsUiEvent

    data object OnViewItemHistoryClick : PassItemDetailsUiEvent

    @JvmInline
    value class OnSharedVaultClick(val sharedVaultId: ShareId) : PassItemDetailsUiEvent

    @JvmInline
    value class OnAttachmentEvent(val attachmentContentEvent: AttachmentContentEvent) : PassItemDetailsUiEvent

    @JvmInline
    value class OnWifiNetworkQRClick(val rawSvg: String) : PassItemDetailsUiEvent

    data object OnShowReusedPasswords : PassItemDetailsUiEvent

    data object OnUpgrade : PassItemDetailsUiEvent
}
