package me.proton.android.pass.navigation.api

import androidx.navigation.NavType

sealed interface NavArgId {
    val key: String
    val navType: NavType<*>
}

enum class CommonNavArgId : NavArgId {
    ItemId {
        override val key: String = "itemId"
        override val navType: NavType<*> = NavType.StringType
    },
    ShareId {
        override val key: String = "shareId"
        override val navType: NavType<*> = NavType.StringType
    }
}

sealed interface OptionalNavArgId {
    val key: String
    val navType: NavType<*>
}

enum class AliasOptionalNavArgId : OptionalNavArgId {
    Title {
        override val key: String = "aliasTitle"
        override val navType: NavType<*> = NavType.StringType
    }
}
