/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.notifications.api

sealed interface InviteNotificationModel {
    val inviterEmail: String

    sealed interface Item : InviteNotificationModel
    sealed interface Vault : InviteNotificationModel

    data class UserItem(override val inviterEmail: String) : Item
    data class UserVault(override val inviterEmail: String) : Vault
    data class GroupItem(override val inviterEmail: String, val groupName: String) : Item
    data class GroupVault(override val inviterEmail: String, val groupName: String) : Vault
}
