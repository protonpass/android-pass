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

package proton.android.pass.features.itemdetail.common

internal interface ItemFeatures {
    val isHistoryEnabled: Boolean
    val isFileAttachmentsEnabled: Boolean
}

internal data class AliasItemFeatures(
    override val isHistoryEnabled: Boolean,
    override val isFileAttachmentsEnabled: Boolean,
    internal val isAliasTrashDialogChecked: Boolean
) : ItemFeatures

internal data class CreditCardItemFeatures(
    override val isHistoryEnabled: Boolean,
    override val isFileAttachmentsEnabled: Boolean
) : ItemFeatures

internal data class NoteItemFeatures(
    override val isHistoryEnabled: Boolean,
    override val isFileAttachmentsEnabled: Boolean
) : ItemFeatures

internal data class LoginItemFeatures(
    override val isHistoryEnabled: Boolean,
    override val isFileAttachmentsEnabled: Boolean
) : ItemFeatures
