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

package proton.android.pass.features.itemcreate.common

import proton.android.pass.features.itemcreate.custom.createupdate.navigation.CreateCustomItemNavItem
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.UpdateCustomItemNavItem
import proton.android.pass.features.itemcreate.identity.navigation.CreateIdentityNavItem
import proton.android.pass.features.itemcreate.identity.navigation.UpdateIdentityNavItem
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.features.itemcreate.login.EditLoginNavItem
import proton.android.pass.features.itemcreate.note.CreateNoteNavItem
import proton.android.pass.features.itemcreate.note.UpdateNoteNavItem
import proton.android.pass.navigation.api.NavItem

enum class CustomFieldPrefix {
    CreateLogin,
    UpdateLogin,
    CreateNote,
    UpdateNote,
    CreateIdentity,
    UpdateIdentity,
    CreateCustomItem,
    UpdateCustomItem
    ;

    companion object {
        fun fromLogin(navItem: NavItem?): CustomFieldPrefix = when (navItem) {
            CreateLoginNavItem -> CreateLogin
            EditLoginNavItem -> UpdateLogin
            else -> {
                throw IllegalArgumentException("Unknown NavItem: $navItem")
            }
        }

        fun fromNote(navItem: NavItem?): CustomFieldPrefix = when (navItem) {
            CreateNoteNavItem -> CreateNote
            UpdateNoteNavItem -> UpdateNote
            else -> {
                throw IllegalArgumentException("Unknown NavItem: $navItem")
            }
        }

        fun fromIdentity(navItem: NavItem?): CustomFieldPrefix = when (navItem) {
            CreateIdentityNavItem -> CreateIdentity
            UpdateIdentityNavItem -> UpdateIdentity
            else -> {
                throw IllegalArgumentException("Unknown NavItem: $navItem")
            }
        }

        fun fromCustomItem(navItem: NavItem?): CustomFieldPrefix = when (navItem) {
            CreateCustomItemNavItem -> CreateCustomItem
            UpdateCustomItemNavItem -> UpdateCustomItem
            else -> {
                throw IllegalArgumentException("Unknown NavItem: $navItem")
            }
        }
    }
}
