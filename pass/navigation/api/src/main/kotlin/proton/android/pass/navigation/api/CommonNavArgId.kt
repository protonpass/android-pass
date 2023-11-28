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

package proton.android.pass.navigation.api

import androidx.navigation.NavType

interface NavArgId {
    val key: String
    val navType: NavType<*>
}

private const val SHARE_ID_KEY = "shareId"
private const val ITEM_ID_KEY = "itemId"

enum class CommonNavArgId : NavArgId {
    ItemId {
        override val key: String = ITEM_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    },
    ShareId {
        override val key: String = SHARE_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    }
}

object DestinationShareNavArgId : NavArgId {
    override val key: String = "destShareId"
    override val navType: NavType<*> = NavType.StringType
}

object ShowUpgradeNavArgId : NavArgId {
    override val key: String = "showUpgrade"
    override val navType: NavType<*> = NavType.StringType
}

interface OptionalNavArgId : NavArgId {
    val default: Any?
        get() = null
}

enum class CommonOptionalNavArgId : OptionalNavArgId {
    ShareId {
        override val key: String = SHARE_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    },
    ItemId {
        override val key: String = ITEM_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    }
}

enum class AliasOptionalNavArgId : OptionalNavArgId {
    Title {
        override val key: String = "aliasTitle"
        override val navType: NavType<*> = NavType.StringType
    },
}
