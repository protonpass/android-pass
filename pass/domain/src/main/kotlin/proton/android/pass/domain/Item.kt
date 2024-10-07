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

package proton.android.pass.domain

import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.hasFlag
import proton.android.pass.common.api.Option
import proton.android.pass.domain.entity.PackageInfo

@JvmInline
value class ItemId(val id: String)

interface ItemBase {
    val id: ItemId
    val userId: UserId
    val itemUuid: String
    val revision: Long
    val shareId: ShareId
    val itemType: ItemType
    val title: EncryptedString
    val note: EncryptedString
    val content: EncryptedByteArray
    val state: Int
    val packageInfoSet: Set<PackageInfo>
    val createTime: Instant
    val modificationTime: Instant
    val lastAutofillTime: Option<Instant>
    val isPinned: Boolean
    val flags: Int

    val hasPasskeys: Boolean
    val hasSkippedHealthCheck: Boolean
    val isEmailBreached: Boolean
    val isAliasDisabled: Boolean
}

data class Item(
    override val id: ItemId,
    override val userId: UserId,
    override val itemUuid: String,
    override val revision: Long,
    override val shareId: ShareId,
    override val itemType: ItemType,
    override val title: EncryptedString,
    override val note: EncryptedString,
    override val content: EncryptedByteArray,
    override val state: Int,
    override val packageInfoSet: Set<PackageInfo>,
    override val createTime: Instant,
    override val modificationTime: Instant,
    override val lastAutofillTime: Option<Instant>,
    override val isPinned: Boolean,
    override val flags: Int
) : ItemBase {
    override val hasPasskeys: Boolean = when (val type = itemType) {
        is ItemType.Login -> type.passkeys.isNotEmpty()
        else -> false
    }

    override val hasSkippedHealthCheck: Boolean = flags.hasFlag(ItemFlag.SkipHealthCheck.value)
    override val isEmailBreached: Boolean = flags.hasFlag(ItemFlag.EmailBreached.value)
    override val isAliasDisabled: Boolean = flags.hasFlag(ItemFlag.AliasDisabled.value)
}
