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

package proton.android.pass.features.item.history.restore

import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreSelection

internal sealed interface ItemHistoryRestoreUiEvent {

    @JvmInline
    value class OnEventConsumed(val event: ItemHistoryRestoreEvent) : ItemHistoryRestoreUiEvent

    data object OnBackClick : ItemHistoryRestoreUiEvent

    data object OnRestoreClick : ItemHistoryRestoreUiEvent

    data class OnRestoreConfirmClick(
        val contents: ItemContents,
        val attachmentsToRestore: Set<AttachmentId>,
        val attachmentsToDelete: Set<AttachmentId>
    ) : ItemHistoryRestoreUiEvent

    data object OnRestoreCancelClick : ItemHistoryRestoreUiEvent

    data class OnSectionClick(
        val section: String,
        val field: ItemDetailsFieldType.Plain
    ) : ItemHistoryRestoreUiEvent

    data class OnHiddenFieldClick(
        val state: HiddenState,
        val field: ItemDetailsFieldType.Hidden
    ) : ItemHistoryRestoreUiEvent

    data class OnHiddenFieldToggle(
        val selection: ItemHistoryRestoreSelection,
        val isVisible: Boolean,
        val hiddenState: HiddenState,
        val fieldType: ItemDetailsFieldType.Hidden,
        val fieldSection: ItemSection
    ) : ItemHistoryRestoreUiEvent

    @JvmInline
    value class OnPasskeyClick(val passkey: UIPasskeyContent) : ItemHistoryRestoreUiEvent

    @JvmInline
    value class OnLinkClick(val linkUrl: String) : ItemHistoryRestoreUiEvent

    @JvmInline
    value class OnWifiNetworkQRClick(val rawSvg: String) : ItemHistoryRestoreUiEvent

    data object OnUpgrade : ItemHistoryRestoreUiEvent
}
