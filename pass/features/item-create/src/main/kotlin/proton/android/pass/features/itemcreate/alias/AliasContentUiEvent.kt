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

package proton.android.pass.features.itemcreate.alias

import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent

internal sealed interface AliasContentUiEvent {

    data object Back : AliasContentUiEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : AliasContentUiEvent

    @JvmInline
    value class OnNoteChange(val note: String) : AliasContentUiEvent

    @JvmInline
    value class OnTitleChange(val title: String) : AliasContentUiEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : AliasContentUiEvent

    @JvmInline
    value class OnPrefixChange(val prefix: String) : AliasContentUiEvent

    data object OnUpgrade : AliasContentUiEvent

    @JvmInline
    value class OnSLNoteChange(val newSLNote: String) : AliasContentUiEvent

    @JvmInline
    value class OnSenderNameChange(val value: String) : AliasContentUiEvent

    data object OnSlNoteInfoClick : AliasContentUiEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : AliasContentUiEvent

    data object DismissAttachmentBanner : AliasContentUiEvent

    data object DismissAdvancedOptionsBanner : AliasContentUiEvent

    data object OnSuffixSelect : AliasContentUiEvent

    data object OnMailboxSelect : AliasContentUiEvent

    @JvmInline
    value class OnCustomFieldEvent(val event: CustomFieldEvent) : AliasContentUiEvent

    data object PasteTotp : AliasContentUiEvent

    @JvmInline
    value class OnScanTotp(val index: Option<Int>) : AliasContentUiEvent
}
